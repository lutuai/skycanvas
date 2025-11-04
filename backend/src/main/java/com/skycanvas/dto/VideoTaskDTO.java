package com.skycanvas.dto;

import lombok.Data;
import java.util.Map;

/**
 * 视频任务DTO（统一格式）
 */
@Data
public class VideoTaskDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 状态：PENDING/PROCESSING/COMPLETED/FAILED
     */
    private String status;

    /**
     * 进度：0-100
     */
    private Integer progress;

    /**
     * 视频URL（完成后）
     */
    private String videoUrl;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 实际时长
     */
    private Integer duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 预计完成时间（秒）
     */
    private Long estimatedTime;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}

