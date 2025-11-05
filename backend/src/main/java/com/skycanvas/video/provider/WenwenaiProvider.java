package com.skycanvas.video.provider;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WenwenAI API 实现（OpenAI chat/completions 兼容格式）
 * 对接第三方中转站：https://breakout.wenwen-ai.com/
 */
@Slf4j
@Service("wenwenaiProvider")
@ConditionalOnProperty(prefix = "video.provider.wenwenai", name = "enabled", havingValue = "true")
public class WenwenaiProvider implements VideoGenerationService {

    @Value("${video.provider.wenwenai.api-key}")
    private String apiKey;

    @Value("${video.provider.wenwenai.base-url}")
    private String baseUrl;

    @Value("${video.provider.wenwenai.model:sora_video2}")
    private String model;

    @Value("${video.provider.wenwenai.timeout:300000}")
    private Integer timeout;

    @Resource
    private RestTemplate restTemplate;

    // 存储任务信息的本地缓存（实际项目中应使用 Redis）
    private final Map<String, VideoTaskDTO> taskCache = new ConcurrentHashMap<>();

    @Override
    public VideoTaskDTO submitTask(VideoGenerationRequest request) {
        log.info("WenwenAI - 提交任务 (OpenAI格式): {}", request.getPrompt());

        try {
            // 构建 OpenAI chat/completions 格式的请求体
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);  // sora_video2
            
            // 构建 messages 数组
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            
            // 构建完整的 content（包含所有参数）
            String content = buildPromptContent(request);
            message.put("content", content);
            
            messages.add(message);
            body.put("messages", messages);

            // 发送请求
            HttpHeaders headers = buildHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            log.info("WenwenAI - 请求体: {}", JSON.toJSONString(body));
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions",
                    entity,
                    String.class
            );

            log.info("WenwenAI - 响应: {}", response.getBody());

            // 解析响应
            JSONObject jsonResponse = JSON.parseObject(response.getBody());
            VideoTaskDTO taskDTO = parseOpenAIResponse(jsonResponse, request);
            
            // 缓存任务信息
            taskCache.put(taskDTO.getTaskId(), taskDTO);
            
            return taskDTO;

        } catch (Exception e) {
            log.error("WenwenAI - 提交任务失败", e);
            throw new RuntimeException("视频生成任务提交失败: " + e.getMessage());
        }
    }

    /**
     * 构建提示词内容（包含所有参数）
     */
    private String buildPromptContent(VideoGenerationRequest request) {
        StringBuilder content = new StringBuilder(request.getPrompt());
        
        // 可以在提示词中添加额外的参数说明
        if (request.getDuration() != null) {
            content.append(String.format(" [时长:%d秒]", request.getDuration()));
        }
        if (request.getResolution() != null) {
            content.append(String.format(" [分辨率:%s]", request.getResolution()));
        }
        if (request.getStyle() != null) {
            content.append(String.format(" [风格:%s]", request.getStyle()));
        }
        if (request.getAspectRatio() != null) {
            content.append(String.format(" [比例:%s]", convertAspectRatio(request.getAspectRatio())));
        }
        
        return content.toString();
    }

    /**
     * 解析 OpenAI 格式的响应
     */
    private VideoTaskDTO parseOpenAIResponse(JSONObject response, VideoGenerationRequest request) {
        VideoTaskDTO dto = new VideoTaskDTO();
        
        // 生成任务ID（使用响应中的 id 或自己生成）
        String taskId = response.getString("id");
        if (taskId == null) {
            taskId = "task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        dto.setTaskId(taskId);
        
        // 解析 choices 中的内容
        JSONArray choices = response.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            
            if (message != null) {
                String content = message.getString("content");
                log.info("WenwenAI - 返回内容: {}", content);
                
                // 尝试从 content 中提取视频URL或任务信息
                parseContentForVideoInfo(dto, content);
            }
        }
        
        // 如果还没有视频URL，设置状态为处理中
        if (dto.getVideoUrl() == null) {
            dto.setStatus("PROCESSING");
            dto.setProgress(0);
        } else {
            dto.setStatus("COMPLETED");
            dto.setProgress(100);
        }
        
        // 添加元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("provider", "wenwenai");
        metadata.put("model", model);
        metadata.put("prompt", request.getPrompt());
        metadata.put("rawResponse", response);
        dto.setMetadata(metadata);
        
        return dto;
    }

    /**
     * 从返回内容中解析视频信息
     */
    private void parseContentForVideoInfo(VideoTaskDTO dto, String content) {
        try {
            // 尝试解析为 JSON
            JSONObject contentJson = JSON.parseObject(content);
            
            // 提取视频URL
            String videoUrl = getStringValue(contentJson, "video_url", "videoUrl", "url", "video");
            if (videoUrl != null) {
                dto.setVideoUrl(videoUrl);
                dto.setStatus("COMPLETED");
                dto.setProgress(100);
            }
            
            // 提取封面URL
            String coverUrl = getStringValue(contentJson, "cover_url", "coverUrl", "thumbnail", "cover");
            dto.setCoverUrl(coverUrl);
            
            // 提取其他信息
            dto.setDuration(contentJson.getInteger("duration"));
            
        } catch (Exception e) {
            // 如果不是 JSON，可能是纯文本或包含URL的文本
            log.debug("内容不是JSON格式，尝试提取URL: {}", content);
            
            // 简单的URL提取（匹配http/https开头的URL）
            if (content.contains("http")) {
                String[] parts = content.split("\\s+");
                for (String part : parts) {
                    if (part.startsWith("http://") || part.startsWith("https://")) {
                        dto.setVideoUrl(part);
                        dto.setStatus("COMPLETED");
                        dto.setProgress(100);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public VideoTaskDTO queryTask(String taskId) {
        log.info("WenwenAI - 查询任务: {}", taskId);

        // 从缓存中获取任务信息
        VideoTaskDTO cachedTask = taskCache.get(taskId);
        if (cachedTask != null) {
            log.info("从缓存获取任务信息: {}", taskId);
            return cachedTask;
        }

        // 如果缓存中没有，返回一个默认的任务状态
        log.warn("任务不存在于缓存中: {}", taskId);
        VideoTaskDTO dto = new VideoTaskDTO();
        dto.setTaskId(taskId);
        dto.setStatus("PENDING");
        dto.setProgress(0);
        dto.setErrorMessage("任务信息未找到");
        
        return dto;
    }

    @Override
    public VideoTaskDTO getResult(String taskId) {
        return queryTask(taskId);
    }

    @Override
    public boolean cancelTask(String taskId) {
        log.info("WenwenAI - 取消任务: {}", taskId);
        
        // OpenAI chat/completions 格式通常是即时返回，不支持取消
        // 从缓存中移除任务
        taskCache.remove(taskId);
        log.info("已从缓存中移除任务: {}", taskId);
        
        return true;
    }

    @Override
    public String getProviderName() {
        return "wenwenai";
    }

    @Override
    public boolean healthCheck() {
        try {
            // 发送一个简单的测试请求
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "health check");
            messages.add(message);
            body.put("messages", messages);

            HttpHeaders headers = buildHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions",
                    entity,
                    String.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("WenwenAI - 健康检查失败", e);
            return false;
        }
    }

    /**
     * 构建请求头
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }

    /**
     * 获取字符串值（支持多个字段名）
     */
    private String getStringValue(JSONObject obj, String... keys) {
        for (String key : keys) {
            String value = obj.getString(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 转换宽高比格式
     * landscape -> 16:9
     * portrait -> 9:16
     * square -> 1:1
     */
    private String convertAspectRatio(String aspectRatio) {
        if (aspectRatio == null) {
            return "16:9";
        }
        switch (aspectRatio.toLowerCase()) {
            case "landscape":
                return "16:9";
            case "portrait":
                return "9:16";
            case "square":
                return "1:1";
            default:
                // 如果已经是比例格式（如 "16:9"），直接返回
                return aspectRatio;
        }
    }
}

