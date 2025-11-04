/**
 * 视频相关API
 */
import { post, get } from '@/utils/request'

/**
 * 生成视频
 */
export function generateVideo(data) {
  return post('/video/generate', data)
}

/**
 * 查询任务状态
 */
export function queryTask(taskId) {
  return get(`/video/task/${taskId}`)
}

/**
 * 获取我的任务列表
 */
export function getMyTasks(params) {
  return get('/video/tasks', params)
}

