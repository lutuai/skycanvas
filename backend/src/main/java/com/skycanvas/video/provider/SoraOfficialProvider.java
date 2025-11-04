package com.skycanvas.video.provider;

import com.skycanvas.dto.VideoGenerationRequest;
import com.skycanvas.dto.VideoTaskDTO;
import com.skycanvas.video.VideoGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 官方Sora API实现（未来切换时使用）
 * 当OpenAI官方Sora API开放后，实现此Provider
 */
@Slf4j
@Service("sora-officialProvider")
@ConditionalOnProperty(prefix = "video.provider.sora-official", name = "enabled", havingValue = "true")
public class SoraOfficialProvider implements VideoGenerationService {

    @Value("${video.provider.sora-official.api-key}")
    private String apiKey;

    @Value("${video.provider.sora-official.base-url}")
    private String baseUrl;

    @Override
    public VideoTaskDTO submitTask(VideoGenerationRequest request) {
        log.info("Sora Official - 提交任务: {}", request.getPrompt());
        
        // TODO: 实现官方API调用逻辑
        // 1. 调用OpenAI官方Sora API
        // 2. 转换为统一格式返回
        
        throw new UnsupportedOperationException("官方Sora API尚未实现，请等待OpenAI开放");
    }

    @Override
    public VideoTaskDTO queryTask(String taskId) {
        log.info("Sora Official - 查询任务: {}", taskId);
        throw new UnsupportedOperationException("官方Sora API尚未实现");
    }

    @Override
    public VideoTaskDTO getResult(String taskId) {
        return queryTask(taskId);
    }

    @Override
    public boolean cancelTask(String taskId) {
        log.info("Sora Official - 取消任务: {}", taskId);
        return false;
    }

    @Override
    public String getProviderName() {
        return "sora-official";
    }

    @Override
    public boolean healthCheck() {
        // TODO: 实现健康检查
        return false;
    }
}

