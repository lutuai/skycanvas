/**
 * 认证相关API
 */
import { post, get, put } from '@/utils/request'

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

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return put('/auth/userinfo', data)
}

/**
 * 发送短信验证码
 */
export function sendSmsCode(phone) {
  return post(`/auth/sms/code?phone=${phone}`)
}

/**
 * 绑定手机号
 */
export function bindPhone(data) {
  return post('/auth/phone/bind', data)
}

/**
 * 刷新Token
 */
export function refreshToken() {
  return post('/auth/token/refresh')
}

