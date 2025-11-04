package com.skycanvas.controller;

import com.skycanvas.common.PageResult;
import com.skycanvas.common.Result;
import com.skycanvas.dto.VideoGenerationRequest;
import com.skycanvas.dto.VideoTaskDTO;
import com.skycanvas.entity.VideoTask;
import com.skycanvas.service.VideoTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 视频生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoTaskService videoTaskService;

    /**
     * 提交视频生成任务
     */
    @PostMapping("/generate")
    public Result<VideoTaskDTO> generateVideo(
            @RequestBody @Validated VideoGenerationRequest request,
            @RequestAttribute("userId") Long userId) {
        
        log.info("用户{}提交视频生成任务: {}", userId, request.getPrompt());
        VideoTaskDTO task = videoTaskService.createTask(request, userId);
        return Result.success("任务提交成功", task);
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/task/{taskId}")
    public Result<VideoTaskDTO> queryTask(
            @PathVariable Long taskId,
            @RequestAttribute("userId") Long userId) {
        
        VideoTaskDTO task = videoTaskService.queryTask(taskId, userId);
        return Result.success(task);
    }

    /**
     * 获取我的任务列表
     */
    @GetMapping("/tasks")
    public Result<PageResult<VideoTask>> getMyTasks(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestAttribute("userId") Long userId) {
        
        PageResult<VideoTask> result = videoTaskService.getUserTasks(userId, current, size);
        return Result.success(result);
    }
}

