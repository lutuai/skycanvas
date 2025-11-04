/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { 
  login as loginApi, 
  getUserInfo as getUserInfoApi,
  updateUserInfo as updateUserInfoApi,
  bindPhone as bindPhoneApi,
  sendSmsCode as sendSmsCodeApi
} from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null,
    isLogin: false,
    loginChecked: false  // 是否已检查过登录状态
  }),
  
  getters: {
    // 是否已登录
    hasLogin: (state) => !!state.token && !!state.userInfo,
    // 积分余额
    credits: (state) => state.userInfo?.credits || 0,
    // 用户昵称
    nickname: (state) => state.userInfo?.nickname || '未登录',
    // 用户头像
    avatar: (state) => state.userInfo?.avatar || '/static/default-avatar.png'
  },
  
  actions: {
    /**
     * 初始化登录状态（自动登录）
     */
    async initLogin() {
      // 如果已经检查过或者没有token，直接返回
      if (this.loginChecked) {
        return
      }
      
      this.loginChecked = true
      
      if (!this.token) {
        console.log('没有登录token，跳过自动登录')
        return
      }
      
      try {
        // 尝试获取用户信息验证token有效性
        await this.loadUserInfo()
        this.isLogin = true
        console.log('自动登录成功')
      } catch (error) {
        console.error('自动登录失败，token可能已过期')
        this.logout()
      }
    },
    
    /**
     * 微信登录
     */
    async login(showToast = true) {
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
        
        if (showToast) {
          const welcomeMsg = data.credits > 0 ? `欢迎回来！您有 ${data.credits} 积分` : '登录成功'
          uni.showToast({
            title: welcomeMsg,
            icon: 'success',
            duration: 2000
          })
        }
        
        return data
      } catch (error) {
        console.error('登录失败:', error)
        if (showToast) {
          uni.showToast({
            title: error.message || '登录失败',
            icon: 'none'
          })
        }
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
      this.loginChecked = false
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    },
    
    /**
     * 检查登录状态
     */
    async checkLogin() {
      if (!this.hasLogin) {
        const confirm = await new Promise((resolve) => {
          uni.showModal({
            title: '提示',
            content: '请先登录',
            confirmText: '去登录',
            success: (res) => resolve(res.confirm)
          })
        })
        
        if (confirm) {
          await this.login()
          return true
        }
        return false
      }
      return true
    },
    
    /**
     * 更新积分
     */
    updateCredits(credits) {
      if (this.userInfo) {
        this.userInfo.credits = credits
        uni.setStorageSync('userInfo', this.userInfo)
      }
    },
    
    /**
     * 更新用户信息
     */
    async updateUserInfo(data) {
      try {
        await updateUserInfoApi(data)
        
        // 重新加载用户信息
        await this.loadUserInfo()
        
        uni.showToast({
          title: '更新成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('更新用户信息失败:', error)
        uni.showToast({
          title: '更新失败',
          icon: 'none'
        })
        throw error
      }
    },
    
    /**
     * 发送短信验证码
     */
    async sendSmsCode(phone) {
      try {
        await sendSmsCodeApi(phone)
        uni.showToast({
          title: '验证码已发送',
          icon: 'success'
        })
      } catch (error) {
        console.error('发送验证码失败:', error)
        throw error
      }
    },
    
    /**
     * 绑定手机号
     */
    async bindPhone(data) {
      try {
        await bindPhoneApi(data)
        
        // 重新加载用户信息
        await this.loadUserInfo()
        
        uni.showToast({
          title: '绑定成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('绑定手机号失败:', error)
        throw error
      }
    }
  }
})

