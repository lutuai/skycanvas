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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        // 4. 获取provider
        VideoGenerationService provider = providerFactory.getProvider();

        // 5. 先保存任务到数据库（使用临时taskId）
        VideoTask task = new VideoTask();
        task.setUserId(userId);
        task.setTaskId("temp_" + UUID.randomUUID().toString().substring(0, 16));  // 临时ID
        task.setProvider(provider.getProviderName());
        task.setPrompt(request.getPrompt());
        task.setParams(JSON.toJSONString(request));
        task.setStatus(0);  // 队列中
        task.setProgress(0);
        task.setCostCredits(requiredCredits);
        videoTaskMapper.insert(task);
        
        Long dbTaskId = task.getId();
        log.info("任务已保存到数据库, ID: {}, userId: {}", dbTaskId, userId);

        // 6. 增加用户生成次数
        userService.incrementTotalVideos(userId);

        // 7. 异步提交到第三方API并更新任务
        submitTaskAsync(dbTaskId, request, provider, userId, requiredCredits);

        // 8. 返回任务信息
        VideoTaskDTO taskDTO = convertToDTO(task);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("dbTaskId", dbTaskId);
        metadata.put("provider", provider.getProviderName());
        taskDTO.setMetadata(metadata);
        
        return taskDTO;
    }

    /**
     * 异步提交任务到第三方API
     */
    @Async
    public void submitTaskAsync(Long dbTaskId, VideoGenerationRequest request, 
                                 VideoGenerationService provider, Long userId, Integer requiredCredits) {
        log.info("异步提交任务到第三方API, dbTaskId: {}", dbTaskId);
        
        try {
            // 提交到第三方API
            VideoTaskDTO taskDTO = provider.submitTask(request);
            
            // 更新数据库中的taskId和状态
            VideoTask task = videoTaskMapper.selectById(dbTaskId);
            if (task != null) {
                task.setTaskId(taskDTO.getTaskId());
                task.setStatus(mapStatusToInt(taskDTO.getStatus()));
                task.setProgress(taskDTO.getProgress() != null ? taskDTO.getProgress() : 0);
                task.setVideoUrl(taskDTO.getVideoUrl());
                task.setCoverUrl(taskDTO.getCoverUrl());
                task.setDuration(taskDTO.getDuration());
                videoTaskMapper.updateById(task);
                
                log.info("任务提交成功, dbTaskId: {}, providerTaskId: {}", dbTaskId, taskDTO.getTaskId());
                
                // 启动状态检查
                checkTaskStatusAsync(dbTaskId, taskDTO.getTaskId(), provider.getProviderName());
            }
        } catch (Exception e) {
            log.error("提交任务到第三方API失败, dbTaskId: {}", dbTaskId, e);
            
            // 更新任务状态为失败
            VideoTask task = videoTaskMapper.selectById(dbTaskId);
            if (task != null) {
                task.setStatus(3);  // 失败
                task.setErrorMsg("提交任务失败: " + e.getMessage());
                videoTaskMapper.updateById(task);
            }
            
            // 退回积分
            creditService.refund(userId, requiredCredits, dbTaskId);
        }
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

