package com.skycanvas.video.provider;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.skycanvas.dto.VideoGenerationRequest;
import com.skycanvas.dto.VideoTaskDTO;
import com.skycanvas.video.VideoGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 第三方Sora中转站实现
 */
@Slf4j
@Service("sora-proxyProvider")
@ConditionalOnProperty(prefix = "video.provider.sora-proxy", name = "enabled", havingValue = "true")
public class SoraProxyProvider implements VideoGenerationService {

    @Value("${video.provider.sora-proxy.api-key}")
    private String apiKey;

    @Value("${video.provider.sora-proxy.base-url}")
    private String baseUrl;

    @Value("${video.provider.sora-proxy.timeout:300000}")
    private Integer timeout;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public VideoTaskDTO submitTask(VideoGenerationRequest request) {
        log.info("Sora Proxy - 提交任务: {}", request.getPrompt());

        try {
            // 构建请求
            Map<String, Object> body = new HashMap<>();
            body.put("prompt", request.getPrompt());
            body.put("duration", request.getDuration());
            body.put("resolution", request.getResolution());
            body.put("style", request.getStyle());
            body.put("aspect_ratio", request.getAspectRatio());

            if (request.getImageUrl() != null) {
                body.put("image_url", request.getImageUrl());
            }

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/video/generate",
                    entity,
                    String.class
            );

            // 解析响应
            JSONObject jsonResponse = JSON.parseObject(response.getBody());
            return convertToTaskDTO(jsonResponse);

        } catch (Exception e) {
            log.error("Sora Proxy - 提交任务失败", e);
            throw new RuntimeException("视频生成任务提交失败: " + e.getMessage());
        }
    }

    @Override
    public VideoTaskDTO queryTask(String taskId) {
        log.info("Sora Proxy - 查询任务: {}", taskId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/v1/video/query/" + taskId,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JSONObject jsonResponse = JSON.parseObject(response.getBody());
            return convertToTaskDTO(jsonResponse);

        } catch (Exception e) {
            log.error("Sora Proxy - 查询任务失败", e);
            throw new RuntimeException("查询任务失败: " + e.getMessage());
        }
    }

    @Override
    public VideoTaskDTO getResult(String taskId) {
        return queryTask(taskId);
    }

    @Override
    public boolean cancelTask(String taskId) {
        log.info("Sora Proxy - 取消任务: {}", taskId);
        // 实现取消逻辑
        return true;
    }

    @Override
    public String getProviderName() {
        return "sora-proxy";
    }

    @Override
    public boolean healthCheck() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/health",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Sora Proxy - 健康检查失败", e);
            return false;
        }
    }

    /**
     * 转换为统一格式的DTO
     */
    private VideoTaskDTO convertToTaskDTO(JSONObject response) {
        VideoTaskDTO dto = new VideoTaskDTO();
        dto.setTaskId(response.getString("task_id"));
        dto.setStatus(mapStatus(response.getString("status")));
        dto.setProgress(response.getInteger("progress"));
        dto.setVideoUrl(response.getString("video_url"));
        dto.setCoverUrl(response.getString("cover_url"));
        dto.setDuration(response.getInteger("duration"));
        dto.setErrorMessage(response.getString("error"));
        dto.setEstimatedTime(response.getLong("estimated_time"));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("provider", "sora-proxy");
        dto.setMetadata(metadata);
        
        return dto;
    }

    /**
     * 状态映射
     */
    private String mapStatus(String status) {
        if (status == null) {
            return "PENDING";
        }
        switch (status.toLowerCase()) {
            case "pending":
            case "queued":
                return "PENDING";
            case "processing":
            case "running":
                return "PROCESSING";
            case "completed":
            case "success":
                return "COMPLETED";
            case "failed":
            case "error":
                return "FAILED";
            default:
                return status.toUpperCase();
        }
    }
}

