/**
 * 认证相关API
 */
import { post, get } from '@/utils/request'

/**
 * 微信登录
 */
export function login(data) {
  return post('/auth/login', data)
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return get('/auth/userinfo')
}

