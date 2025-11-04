package com.skycanvas.video;

import com.skycanvas.dto.VideoGenerationRequest;
import com.skycanvas.dto.VideoTaskDTO;

/**
 * 视频生成服务统一接口
 * 所有provider必须实现此接口，确保无缝切换
 */
public interface VideoGenerationService {

    /**
     * 提交视频生成任务
     * @param request 统一请求对象（内部转换为各provider格式）
     * @return 任务信息（统一格式）
     */
    VideoTaskDTO submitTask(VideoGenerationRequest request);

    /**
     * 查询任务状态
     * @param taskId 任务ID（provider的原始ID）
     * @return 任务状态（统一格式）
     */
    VideoTaskDTO queryTask(String taskId);

    /**
     * 获取生成结果
     * @param taskId 任务ID
     * @return 视频URL和元数据
     */
    VideoTaskDTO getResult(String taskId);

    /**
     * 取消任务
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean cancelTask(String taskId);

    /**
     * 获取服务提供商名称
     * @return provider名称
     */
    String getProviderName();

    /**
     * 健康检查
     * @return 是否可用
     */
    boolean healthCheck();
}

