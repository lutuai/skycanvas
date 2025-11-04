/**
 * 积分相关API
 */
import { get } from '@/utils/request'

/**
 * 获取积分余额
 */
export function getBalance() {
  return get('/credit/balance')
}

/**
 * 获取积分记录
 */
export function getCreditLogs(params) {
  return get('/credit/logs', params)
}

