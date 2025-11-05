<template>
  <view class="container">
    <view class="login-card">
      <view class="logo-area">
        <text class="logo">🎨</text>
        <text class="app-name">SkyCanvas</text>
        <text class="slogan">AI赋能，创作无限</text>
      </view>

      <!-- 登录方式切换 -->
      <view class="tab-bar">
        <view 
          :class="['tab-item', loginType === 'phone' ? 'active' : '']"
          @click="loginType = 'phone'"
        >
          手机号登录
        </view>
        <view 
          :class="['tab-item', loginType === 'quick' ? 'active' : '']"
          @click="loginType = 'quick'"
        >
          快速登录
        </view>
      </view>

      <!-- 手机号登录 -->
      <view v-if="loginType === 'phone'" class="login-form">
        <view class="input-group">
          <input 
            v-model="phone"
            class="input-dark" 
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
          />
        </view>
        
        <view class="input-group">
          <view class="code-input-wrapper">
            <input 
              v-model="code"
              class="input-dark code-input" 
              type="number"
              maxlength="6"
              placeholder="请输入验证码"
            />
            <button 
              class="btn-code"
              :disabled="codeSending || countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </button>
          </view>
        </view>

        <button 
          class="btn-primary btn-login"
          :disabled="!canLogin"
          @click="handlePhoneLogin"
        >
          {{ loggingIn ? '登录中...' : '登录' }}
        </button>

        <view class="tips">
          <text>未注册手机号将自动创建账号</text>
        </view>
      </view>

      <!-- 快速登录 -->
      <view v-else class="login-form">
        <!-- #ifdef MP-WEIXIN -->
        <button 
          class="btn-primary btn-login"
          :disabled="loggingIn"
          @click="handleWeixinLogin"
        >
          <text>{{ loggingIn ? '登录中...' : '微信一键登录' }}</text>
        </button>
        <!-- #endif -->

        <!-- #ifdef H5 -->
        <button 
          class="btn-primary btn-login"
          :disabled="loggingIn"
          @click="handleH5Login"
        >
          <text>{{ loggingIn ? '登录中...' : '游客登录' }}</text>
        </button>
        <view class="tips">
          <text>H5环境使用设备ID登录</text>
        </view>
        <!-- #endif -->
      </view>

      <!-- 协议提示 -->
      <view class="agreement">
        <text>登录即表示同意</text>
        <text class="link" @click="showAgreement('user')">《用户协议》</text>
        <text>和</text>
        <text class="link" @click="showAgreement('privacy')">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 登录方式：phone-手机号 quick-快速登录
const loginType = ref('quick')

// 手机号登录相关
const phone = ref('')
const code = ref('')
const codeSending = ref(false)
const countdown = ref(0)
const loggingIn = ref(false)

// 计算属性：是否可以登录
const canLogin = computed(() => {
  // 手机号和验证码都不为空，且未在登录中
  return phone.value.trim() !== '' && 
         code.value.trim() !== '' && 
         phone.value.length === 11 &&
         code.value.length === 6 &&
         !loggingIn.value
})

// 发送验证码
const handleSendCode = async () => {
  if (!phone.value) {
    uni.showToast({
      title: '请输入手机号',
      icon: 'none'
    })
    return
  }

  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    uni.showToast({
      title: '手机号格式不正确',
      icon: 'none'
    })
    return
  }

  try {
    codeSending.value = true
    await userStore.sendSmsCode(phone.value)
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    codeSending.value = false
  }
}

// 手机号登录
const handlePhoneLogin = async () => {
  if (!phone.value || !code.value) {
    uni.showToast({
      title: '请输入手机号和验证码',
      icon: 'none'
    })
    return
  }

  try {
    loggingIn.value = true
    await userStore.loginByPhoneCode(phone.value, code.value)
    
    // 登录成功，返回上一页或首页
    setTimeout(() => {
      uni.navigateBack({
        fail: () => {
          uni.switchTab({
            url: '/pages/index/index'
          })
        }
      })
    }, 1500)
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loggingIn.value = false
  }
}

// 微信登录
const handleWeixinLogin = async () => {
  try {
    loggingIn.value = true
    await userStore.loginByWeixin()
    
    // 登录成功，返回上一页或首页
    setTimeout(() => {
      uni.navigateBack({
        fail: () => {
          uni.switchTab({
            url: '/pages/index/index'
          })
        }
      })
    }, 1500)
  } catch (error) {
    console.error('微信登录失败:', error)
  } finally {
    loggingIn.value = false
  }
}

// H5登录
const handleH5Login = async () => {
  try {
    loggingIn.value = true
    await userStore.loginByH5()
    
    // 登录成功，返回上一页或首页
    setTimeout(() => {
      uni.navigateBack({
        fail: () => {
          uni.switchTab({
            url: '/pages/index/index'
          })
        }
      })
    }, 1500)
  } catch (error) {
    console.error('H5登录失败:', error)
  } finally {
    loggingIn.value = false
  }
}

// 显示协议
const showAgreement = (type) => {
  uni.showModal({
    title: type === 'user' ? '用户协议' : '隐私政策',
    content: '这里是协议内容...',
    showCancel: false
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #0a0a0a 0%, #1a1a1a 100%);
  padding: 80rpx 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 600rpx;
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  border-radius: 32rpx;
  padding: 60rpx 50rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.5);
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
  
  .logo {
    font-size: 100rpx;
    margin-bottom: 20rpx;
  }
  
  .app-name {
    font-size: 48rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 10rpx;
  }
  
  .slogan {
    font-size: 24rpx;
    color: #999;
  }
}

.tab-bar {
  display: flex;
  gap: 20rpx;
  margin-bottom: 40rpx;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 20rpx;
    font-size: 28rpx;
    color: #999;
    border-bottom: 4rpx solid transparent;
    cursor: pointer;
    transition: all 0.3s;
    
    &.active {
      color: var(--primary-color);
      border-bottom-color: var(--primary-color);
    }
  }
}

.login-form {
  .input-group {
    margin-bottom: 30rpx;
  }
  
  .code-input-wrapper {
    display: flex;
    gap: 20rpx;
    
    .code-input {
      flex: 1;
    }
    
    .btn-code {
      flex-shrink: 0;
      width: 200rpx;
      height: 80rpx;
      line-height: 80rpx;
      padding: 0;
      background: transparent;
      border: 2rpx solid var(--primary-color);
      color: var(--primary-color);
      border-radius: 12rpx;
      font-size: 24rpx;
      
      &:disabled {
        opacity: 0.5;
        border-color: #666;
        color: #666;
      }
    }
  }
  
  .btn-login {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    font-size: 32rpx;
    margin-top: 40rpx;
    transition: all 0.3s;
    
    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
      background: #666 !important;
    }
  }
  
  .tips {
    margin-top: 20rpx;
    text-align: center;
    font-size: 24rpx;
    color: #999;
  }
}

.agreement {
  margin-top: 60rpx;
  text-align: center;
  font-size: 22rpx;
  color: #999;
  
  .link {
    color: var(--primary-color);
    margin: 0 5rpx;
  }
}

.input-dark {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background: var(--bg-input);
  border: 2rpx solid var(--border-color);
  border-radius: 12rpx;
  color: var(--text-primary);
  padding: 0 30rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
</style>

