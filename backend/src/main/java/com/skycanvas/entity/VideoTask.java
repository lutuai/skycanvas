package com.skycanvas.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频生成任务实体
 */
@Data
@TableName("tb_video_task")
public class VideoTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务ID（第三方API返回）
     */
    private String taskId;

    /**
     * 服务提供商
     */
    private String provider;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 生成参数（JSON格式）
     */
    private String params;

    /**
     * 任务状态：0-队列中，1-生成中，2-成功，3-失败
     */
    private Integer status;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 消耗积分
     */
    private Integer costCredits;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
}

