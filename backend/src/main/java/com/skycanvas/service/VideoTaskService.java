package com.skycanvas.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skycanvas.common.PageResult;
import com.skycanvas.dto.VideoGenerationRequest;
import com.skycanvas.dto.VideoTaskDTO;
import com.skycanvas.entity.VideoTask;
import com.skycanvas.mapper.VideoTaskMapper;
import com.skycanvas.video.VideoGenerationService;
import com.skycanvas.video.VideoProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 视频任务服务
 */
@Slf4j
@Service
public class VideoTaskService {

    @Autowired
    private VideoTaskMapper videoTaskMapper;

    @Autowired
    private VideoProviderFactory providerFactory;

    @Autowired
    private CreditService creditService;

    @Autowired
    private UserService userService;

    /**
     * 创建视频生成任务
     */
    @Transactional(rollbackFor = Exception.class)
    public VideoTaskDTO createTask(VideoGenerationRequest request, Long userId) {
        // 1. 计算所需积分
        Integer requiredCredits = calculateCredits(request);

        // 2. 检查积分余额
        Integer balance = creditService.getBalance(userId);
        if (balance < requiredCredits) {
            throw new RuntimeException("积分不足，需要" + requiredCredits + "积分");
        }

        // 3. 扣除积分
        creditService.consume(userId, requiredCredits, null, "生成" + request.getDuration() + "秒视频");

        // 4. 获取provider并提交任务
        VideoGenerationService provider = providerFactory.getProvider();
        VideoTaskDTO taskDTO;
        
        try {
            taskDTO = provider.submitTask(request);
        } catch (Exception e) {
            // 提交失败，退回积分
            creditService.refund(userId, requiredCredits, null);
            throw new RuntimeException("提交任务失败: " + e.getMessage());
        }

        // 5. 保存任务到数据库
        VideoTask task = new VideoTask();
        task.setUserId(userId);
        task.setTaskId(taskDTO.getTaskId());
        task.setProvider(provider.getProviderName());
        task.setPrompt(request.getPrompt());
        task.setParams(JSON.toJSONString(request));
        task.setStatus(0);  // 队列中
        task.setProgress(0);
        task.setCostCredits(requiredCredits);
        videoTaskMapper.insert(task);

        // 更新积分日志中的taskId
        Long taskId = task.getId();
        taskDTO.setMetadata(taskDTO.getMetadata());
        taskDTO.getMetadata().put("dbTaskId", taskId);

        // 6. 异步检查任务状态
        checkTaskStatusAsync(taskId, taskDTO.getTaskId(), provider.getProviderName());

        // 7. 增加用户生成次数
        userService.incrementTotalVideos(userId);

        return taskDTO;
    }

    /**
     * 查询任务状态
     */
    public VideoTaskDTO queryTask(Long taskId, Long userId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在");
        }

        // 如果任务还在进行中，查询最新状态
        if (task.getStatus() == 0 || task.getStatus() == 1) {
            try {
                VideoGenerationService provider = providerFactory.getProvider(task.getProvider());
                VideoTaskDTO dto = provider.queryTask(task.getTaskId());
                
                // 更新数据库
                updateTaskFromDTO(task, dto);
                
                return dto;
            } catch (Exception e) {
                log.error("查询任务状态失败", e);
            }
        }

        // 返回数据库中的状态
        return convertToDTO(task);
    }

    /**
     * 获取用户的任务列表
     */
    public PageResult<VideoTask> getUserTasks(Long userId, Long current, Long size) {
        Page<VideoTask> page = new Page<>(current, size);
        LambdaQueryWrapper<VideoTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoTask::getUserId, userId);
        wrapper.orderByDesc(VideoTask::getCreateTime);

        Page<VideoTask> result = videoTaskMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), current, size);
    }

    /**
     * 异步检查任务状态
     */
    @Async
    public void checkTaskStatusAsync(Long taskId, String providerTaskId, String providerName) {
        log.info("开始异步检查任务状态: taskId={}, providerTaskId={}", taskId, providerTaskId);
        
        try {
            VideoGenerationService provider = providerFactory.getProvider(providerName);
            VideoTask task = videoTaskMapper.selectById(taskId);
            
            // 最多检查60次，每5秒一次（总共5分钟）
            for (int i = 0; i < 60; i++) {
                Thread.sleep(5000);  // 5秒
                
                VideoTaskDTO dto = provider.queryTask(providerTaskId);
                updateTaskFromDTO(task, dto);
                
                // 如果任务完成或失败，停止检查
                if ("COMPLETED".equals(dto.getStatus()) || "FAILED".equals(dto.getStatus())) {
                    log.info("任务{}状态更新完成: {}", taskId, dto.getStatus());
                    
                    // 如果失败，退回积分
                    if ("FAILED".equals(dto.getStatus())) {
                        creditService.refund(task.getUserId(), task.getCostCredits(), taskId);
                    }
                    
                    break;
                }
            }
        } catch (Exception e) {
            log.error("异步检查任务状态失败", e);
        }
    }

    /**
     * 计算所需积分
     */
    private Integer calculateCredits(VideoGenerationRequest request) {
        int baseCredits = 10;  // 基础积分
        
        // 根据时长计算
        if (request.getDuration() != null) {
            baseCredits = request.getDuration() * 2;
        }
        
        // 高清加价
        if ("1080p".equals(request.getResolution())) {
            baseCredits += 5;
        }
        
        return baseCredits;
    }

    /**
     * 更新任务状态
     */
    private void updateTaskFromDTO(VideoTask task, VideoTaskDTO dto) {
        task.setStatus(mapStatusToInt(dto.getStatus()));
        task.setProgress(dto.getProgress());
        task.setVideoUrl(dto.getVideoUrl());
        task.setCoverUrl(dto.getCoverUrl());
        task.setDuration(dto.getDuration());
        task.setErrorMsg(dto.getErrorMessage());
        
        if ("COMPLETED".equals(dto.getStatus())) {
            task.setCompleteTime(LocalDateTime.now());
        }
        
        videoTaskMapper.updateById(task);
    }

    /**
     * 状态字符串转数字
     */
    private Integer mapStatusToInt(String status) {
        switch (status) {
            case "PENDING":
                return 0;
            case "PROCESSING":
                return 1;
            case "COMPLETED":
                return 2;
            case "FAILED":
                return 3;
            default:
                return 0;
        }
    }

    /**
     * 转换为DTO
     */
    private VideoTaskDTO convertToDTO(VideoTask task) {
        VideoTaskDTO dto = new VideoTaskDTO();
        dto.setTaskId(task.getTaskId());
        dto.setStatus(mapIntToStatus(task.getStatus()));
        dto.setProgress(task.getProgress());
        dto.setVideoUrl(task.getVideoUrl());
        dto.setCoverUrl(task.getCoverUrl());
        dto.setDuration(task.getDuration());
        dto.setErrorMessage(task.getErrorMsg());
        return dto;
    }

    /**
     * 状态数字转字符串
     */
    private String mapIntToStatus(Integer status) {
        switch (status) {
            case 0:
                return "PENDING";
            case 1:
                return "PROCESSING";
            case 2:
                return "COMPLETED";
            case 3:
                return "FAILED";
            default:
                return "PENDING";
        }
    }
}

