/**
 * 网络请求封装
 */

const BASE_URL = process.env.NODE_ENV === 'development' 
  ? 'http://localhost:8080/api' 
  : 'https://your-domain.com/api'

/**
 * 请求拦截器
 * 
 * 职责：
 * 1. 自动在请求头中添加 JWT Token
 * 2. 后端通过 Token 识别用户身份，无需前端传递 userId
 * 3. 保证安全性：用户ID由后端从 Token 中解析，防止伪造
 */
function requestInterceptor(config) {
  // 添加 JWT Token 到请求头
  const token = uni.getStorageSync('token')
  if (token) {
    config.header = config.header || {}
    config.header['Authorization'] = `Bearer ${token}`
  }
  return config
}

/**
 * 响应拦截器
 */
function responseInterceptor(response) {
  const { statusCode, data } = response
  
  // HTTP状态码错误
  if (statusCode !== 200) {
    // Token过期，跳转登录
    if (statusCode === 401) {
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/index'
        })
      }, 1500)
    } else {
      uni.showToast({
        title: `请求失败(${statusCode})`,
        icon: 'none'
      })
    }
    return Promise.reject(new Error(`HTTP ${statusCode}`))
  }
  
  // 业务状态码错误
  if (data.code !== 200) {
    uni.showToast({
      title: data.message || '操作失败',
      icon: 'none',
      duration: 2000
    })
    return Promise.reject(new Error(data.message || '操作失败'))
  }
  
  // 成功，返回数据
  return Promise.resolve(data.data)
}

/**
 * 发起请求
 */
export function request(options) {
  // 应用请求拦截器
  const config = requestInterceptor({
    url: BASE_URL + options.url,
    method: options.method || 'GET',
    data: options.data || {},
    header: {
      'Content-Type': 'application/json',
      ...options.header
    },
    timeout: options.timeout || 30000
  })
  
  return new Promise((resolve, reject) => {
    uni.request({
      ...config,
      success: (res) => {
        // 响应拦截器现在始终返回 Promise
        responseInterceptor(res)
          .then(resolve)
          .catch(reject)
      },
      fail: (err) => {
        console.error('网络请求失败:', err)
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(new Error('网络连接失败'))
      }
    })
  })
}

/**
 * GET请求
 */
export function get(url, data, options = {}) {
  return request({
    url,
    method: 'GET',
    data,
    ...options
  })
}

/**
 * POST请求
 */
export function post(url, data, options = {}) {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT请求
 */
export function put(url, data, options = {}) {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE请求
 */
export function del(url, data, options = {}) {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}

