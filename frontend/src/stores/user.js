/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { 
  login as loginApi, 
  getUserInfo as getUserInfoApi,
  updateUserInfo as updateUserInfoApi,
  bindPhone as bindPhoneApi,
  sendSmsCode as sendSmsCodeApi,
  loginByPhone as loginByPhoneApi
} from '@/api/auth'
import { getUserAvatar } from '@/utils/avatar'
import { showCustomModal } from '@/utils/modal'

export const useUserStore = defineStore('user', {
  state: () => {
    const token = uni.getStorageSync('token') || ''
    const userInfo = uni.getStorageSync('userInfo') || null
    // 如果有 token 和 userInfo，初始化为已登录状态
    const isLogin = !!(token && userInfo)
    
    return {
      token,
      userInfo,
      isLogin,
      loginChecked: false  // 是否已检查过登录状态
    }
  },
  
  getters: {
    // 是否已登录
    hasLogin: (state) => !!state.token && !!state.userInfo,
    // 积分余额
    credits: (state) => state.userInfo?.credits || 0,
    // 用户昵称
    nickname: (state) => state.userInfo?.nickname || '未登录',
    // 用户头像（智能生成默认头像）
    avatar: (state) => getUserAvatar(state.userInfo, 'svg')
  },
  
  actions: {
    /**
     * 初始化登录状态（自动登录）
     */
    async initLogin() {
      // 如果已经检查过，直接返回
      if (this.loginChecked) {
        console.log('已检查过登录状态，跳过')
        return
      }
      
      this.loginChecked = true
      
      // 如果没有token，尝试静默登录
      if (!this.token) {
        console.log('没有登录token，尝试自动登录')
        try {
          // #ifdef MP-WEIXIN
          // 小程序环境：静默登录（无需用户授权）
          await this.silentLoginByWeixin()
          console.log('小程序静默登录成功')
          return
          // #endif
          
          // #ifdef H5
          // H5环境：自动游客登录
          await this.loginByH5(false)
          console.log('H5自动登录成功')
          return
          // #endif
        } catch (error) {
          console.error('自动登录失败:', error)
          this.isLogin = false
          return
        }
      }
      
      // 如果有 token 和 userInfo，先标记为已登录（避免页面闪烁）
      if (this.token && this.userInfo) {
        this.isLogin = true
        console.log('从缓存恢复登录状态')
      }
      
      try {
        // 后台验证token有效性并刷新用户信息
        await this.loadUserInfo()
        this.isLogin = true
        console.log('自动登录验证成功')
      } catch (error) {
        console.error('Token验证失败，可能已过期:', error)
        // Token失效，清除登录状态并尝试重新登录
        this.logout()
        // 重新尝试自动登录
        await this.initLogin()
      }
    },
    
    /**
     * 登录（支持多平台）
     */
    async login(showToast = true) {
      try {
        // #ifdef MP-WEIXIN
        // 微信小程序登录
        return await this.loginByWeixin(showToast)
        // #endif
        
        // #ifdef H5
        // H5登录 - 使用测试账号或手机号登录
        return await this.loginByH5(showToast)
        // #endif
        
        // #ifndef MP-WEIXIN || H5
        // 其他平台暂不支持
        throw new Error('当前平台暂不支持登录')
        // #endif
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
     * 微信小程序静默登录（不获取用户信息）
     */
    async silentLoginByWeixin(showToast = false) {
      try {
        // 获取微信code（无需用户授权）
        const { code } = await uni.login({
          provider: 'weixin'
        })
        
        // 调用登录接口，使用空的昵称和头像（后端会自动生成）
        const data = await loginApi({
          code,
          nickname: '',
          avatar: ''
        })
        
        // 保存登录信息
        this.saveLoginData(data, showToast)
        return data
      } catch (error) {
        console.error('静默登录失败:', error)
        throw error
      }
    },
    
    /**
     * 微信小程序登录（获取用户信息 - 用于完善资料）
     */
    async loginByWeixin(showToast = true) {
      try {
        // 获取微信code
        const { code } = await uni.login({
          provider: 'weixin'
        })
        
        // 获取用户信息（需要用户授权）
        let nickname = ''
        let avatar = ''
        
        try {
          const { userInfo } = await uni.getUserProfile({
            desc: '用于完善用户资料'
          })
          nickname = userInfo.nickName
          avatar = userInfo.avatarUrl
        } catch (e) {
          console.log('用户取消授权，使用默认信息')
          // 用户取消授权，使用空信息（后端会生成默认值）
        }
        
        // 调用登录接口
        const data = await loginApi({
          code,
          nickname,
          avatar
        })
        
        // 保存登录信息
        this.saveLoginData(data, showToast)
        return data
      } catch (error) {
        console.error('微信登录失败:', error)
        throw error
      }
    },
    
    /**
     * H5登录（使用测试账号）
     */
    async loginByH5(showToast = true) {
      // H5环境使用模拟登录，生成一个唯一的设备ID
      let deviceId = uni.getStorageSync('deviceId')
      if (!deviceId) {
        deviceId = 'h5_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
        uni.setStorageSync('deviceId', deviceId)
      }
      
      // 调用登录接口，使用设备ID作为code（不传avatar，让系统自动生成）
      const data = await loginApi({
        code: deviceId,
        nickname: '访客用户',
        avatar: '' // 空字符串，后续会自动生成默认头像
      })
      
      // 保存登录信息
      this.saveLoginData(data, showToast)
      return data
    },
    
    /**
     * 手机号验证码登录
     */
    async loginByPhoneCode(phone, code, showToast = true) {
      try {
        const data = await loginByPhoneApi({
          phone,
          code
        })
        
        // 保存登录信息
        this.saveLoginData(data, showToast)
        return data
      } catch (error) {
        console.error('手机号登录失败:', error)
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
     * 保存登录数据
     */
    saveLoginData(data, showToast = true) {
      // 确保响应式更新
      this.token = data.token
      this.userInfo = { ...data }  // 使用展开运算符确保响应式
      this.isLogin = true
      this.loginChecked = true
      
      // 持久化存储
      uni.setStorageSync('token', data.token)
      uni.setStorageSync('userInfo', data)
      
      // 显示提示
      if (showToast) {
        const welcomeMsg = data.credits > 0 ? `欢迎！您有 ${data.credits} 积分` : '登录成功'
        uni.showToast({
          title: welcomeMsg,
          icon: 'success',
          duration: 2000
        })
      }
      
      // 延迟刷新页面，确保状态已更新
      setTimeout(() => {
        // 触发页面刷新
        uni.$emit('userLoginSuccess')
      }, 100)
    },
    
    /**
     * 加载用户信息
     * @param {boolean} autoLogout - 失败时是否自动登出
     */
    async loadUserInfo(autoLogout = false) {
      try {
        const data = await getUserInfoApi()
        // 确保响应式更新
        this.userInfo = { ...data }
        this.isLogin = true
        uni.setStorageSync('userInfo', data)
        return data
      } catch (error) {
        console.error('加载用户信息失败:', error)
        // 如果需要自动登出（非 initLogin 调用时）
        if (autoLogout) {
          this.logout()
        }
        throw error
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
        const confirm = await showCustomModal({
          title: '提示',
          content: '请先登录',
          confirmText: '去登录'
        })
        
        if (confirm) {
          // 跳转到登录页面
          uni.navigateTo({
            url: '/pages/login/index'
          })
          return false
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
        await this.loadUserInfo(true)
        
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
        await this.loadUserInfo(true)
        
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

