package com.skycanvas.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Map;

/**
 * 视频生成请求DTO
 */
@Data
public class VideoGenerationRequest {

    /**
     * 提示词（必填）
     */
    @NotBlank(message = "提示词不能为空")
    private String prompt;

    /**
     * 上传的图片URL（图生视频时使用）
     */
    private String imageUrl;

    /**
     * 时长（秒）：2/5/10
     */
    @Min(value = 2, message = "时长最少2秒")
    @Max(value = 10, message = "时长最多10秒")
    private Integer duration = 5;

    /**
     * 分辨率：720p/1080p
     */
    private String resolution = "720p";

    /**
     * 风格：realistic/anime/artistic
     */
    private String style = "realistic";

    /**
     * 横竖屏：landscape/portrait/square
     */
    private String aspectRatio = "landscape";

    /**
     * 创意度：0.0-1.0
     */
    @Min(value = 0, message = "创意度最小为0")
    @Max(value = 1, message = "创意度最大为1")
    private Double temperature = 0.7;

    /**
     * 扩展参数
     */
    private Map<String, Object> extraParams;
}

