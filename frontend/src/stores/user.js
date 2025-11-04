/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null,
    isLogin: false
  }),
  
  getters: {
    // 是否已登录
    hasLogin: (state) => !!state.token,
    // 积分余额
    credits: (state) => state.userInfo?.credits || 0
  },
  
  actions: {
    /**
     * 微信登录
     */
    async login() {
      try {
        // 获取微信code
        const { code } = await uni.login({
          provider: 'weixin'
        })
        
        // 获取用户信息
        let nickname = ''
        let avatar = ''
        
        // #ifdef MP-WEIXIN
        // 微信小程序需要用户授权才能获取信息
        try {
          const { userInfo } = await uni.getUserProfile({
            desc: '用于完善用户资料'
          })
          nickname = userInfo.nickName
          avatar = userInfo.avatarUrl
        } catch (e) {
          console.log('用户取消授权')
        }
        // #endif
        
        // 调用登录接口
        const data = await loginApi({
          code,
          nickname,
          avatar
        })
        
        // 保存token和用户信息
        this.token = data.token
        this.userInfo = data
        this.isLogin = true
        
        uni.setStorageSync('token', data.token)
        uni.setStorageSync('userInfo', data)
        
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
        
        return data
      } catch (error) {
        console.error('登录失败:', error)
        uni.showToast({
          title: '登录失败',
          icon: 'none'
        })
        throw error
      }
    },
    
    /**
     * 加载用户信息
     */
    async loadUserInfo() {
      try {
        const data = await getUserInfoApi()
        this.userInfo = data
        this.isLogin = true
        uni.setStorageSync('userInfo', data)
        return data
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.logout()
      }
    },
    
    /**
     * 登出
     */
    logout() {
      this.token = ''
      this.userInfo = null
      this.isLogin = false
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    },
    
    /**
     * 更新积分
     */
    updateCredits(credits) {
      if (this.userInfo) {
        this.userInfo.credits = credits
        uni.setStorageSync('userInfo', this.userInfo)
      }
    }
  }
})

