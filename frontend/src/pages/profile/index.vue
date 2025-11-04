<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-info" @click="goEditProfile">
        <image 
          :src="userInfo?.avatar || '/static/default-avatar.png'" 
          class="avatar"
        />
        <view class="info">
          <view class="nickname">{{ userInfo?.nickname || '未登录' }}</view>
          <view class="stats">
            <text>已生成 {{ userInfo?.totalVideos || 0 }} 个视频</text>
          </view>
        </view>
        <text class="edit-icon">✏️</text>
      </view>
      
      <!-- 积分卡片 -->
      <view class="credit-card">
        <view class="credit-info">
          <text class="credit-label">积分余额</text>
          <text class="credit-value">{{ userInfo?.credits || 0 }}</text>
        </view>
        <button class="btn-ghost btn-recharge" @click="goRecharge">
          充值
        </button>
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="goWorks">
        <view class="menu-left">
          <text class="menu-icon">🎬</text>
          <text class="menu-text">我的作品</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      
      <view class="menu-item" @click="goCreditLogs">
        <view class="menu-left">
          <text class="menu-icon">💰</text>
          <text class="menu-text">积分明细</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      
      <view class="menu-item" @click="goHistory">
        <view class="menu-left">
          <text class="menu-icon">📝</text>
          <text class="menu-text">生成历史</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 其他设置 -->
    <view class="menu-list">
      <view class="menu-item" @click="handleAbout">
        <view class="menu-left">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-text">关于我们</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      
      <view class="menu-item" @click="handleContact">
        <view class="menu-left">
          <text class="menu-icon">📞</text>
          <text class="menu-text">联系客服</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 登出按钮 -->
    <view v-if="userStore.hasLogin" class="logout-area">
      <button class="btn-ghost btn-logout" @click="handleLogout">
        退出登录
      </button>
    </view>
    
    <!-- 未登录按钮 -->
    <view v-else class="logout-area">
      <button class="btn-primary btn-login" @click="handleLogin">
        立即登录
      </button>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

// 刷新用户信息
onMounted(() => {
  if (userStore.hasLogin) {
    userStore.loadUserInfo()
  }
})

// 去充值
const goRecharge = async () => {
  if (!await userStore.checkLogin()) {
    return
  }
  uni.navigateTo({
    url: '/pages/credit/recharge'
  })
}

// 我的作品
const goWorks = async () => {
  if (!await userStore.checkLogin()) {
    return
  }
  uni.switchTab({
    url: '/pages/works/index'
  })
}

// 积分明细
const goCreditLogs = async () => {
  if (!await userStore.checkLogin()) {
    return
  }
  uni.navigateTo({
    url: '/pages/credit/logs'
  })
}

// 生成历史
const goHistory = async () => {
  if (!await userStore.checkLogin()) {
    return
  }
  uni.switchTab({
    url: '/pages/generate/index'
  })
}

// 编辑个人信息
const goEditProfile = async () => {
  if (!await userStore.checkLogin()) {
    return
  }
  uni.navigateTo({
    url: '/pages/profile/edit'
  })
}

// 关于我们
const handleAbout = () => {
  uni.showModal({
    title: '关于SkyCanvas',
    content: 'SkyCanvas是一款基于AI技术的视频生成平台，让创作变得更简单。',
    showCancel: false
  })
}

// 联系客服
const handleContact = () => {
  uni.showModal({
    title: '联系客服',
    content: '客服微信：skycanvas_support',
    showCancel: false
  })
}

// 登录
const handleLogin = async () => {
  try {
    await userStore.login()
  } catch (error) {
    console.error('登录失败:', error)
  }
}

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #0a0a0a;
  padding: 40rpx;
}

.user-card {
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 40rpx;
    cursor: pointer;
    
    .avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 60rpx;
      margin-right: 30rpx;
    }
    
    .info {
      flex: 1;
      
      .nickname {
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
        margin-bottom: 10rpx;
      }
      
      .stats {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .edit-icon {
      font-size: 32rpx;
      opacity: 0.6;
    }
  }
  
  .credit-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    background: rgba(0, 217, 163, 0.1);
    border-radius: 16rpx;
    border: 2rpx solid rgba(0, 217, 163, 0.3);
    
    .credit-info {
      display: flex;
      flex-direction: column;
      
      .credit-label {
        font-size: 24rpx;
        color: #999;
        margin-bottom: 10rpx;
      }
      
      .credit-value {
        font-size: 48rpx;
        font-weight: bold;
        color: #00d9a3;
      }
    }
    
    .btn-recharge {
      width: 160rpx;
      height: 60rpx;
      line-height: 60rpx;
      border-radius: 30rpx;
      font-size: 28rpx;
      padding: 0;
    }
  }
}

.menu-list {
  background: #1a1a1a;
  border-radius: 20rpx;
  margin-bottom: 40rpx;
  overflow: hidden;
  
  .menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 40rpx 30rpx;
    border-bottom: 1rpx solid #2a2a2a;
    
    &:last-child {
      border-bottom: none;
    }
    
    .menu-left {
      display: flex;
      align-items: center;
      
      .menu-icon {
        font-size: 40rpx;
        margin-right: 20rpx;
      }
      
      .menu-text {
        font-size: 30rpx;
        color: #fff;
      }
    }
    
    .menu-arrow {
      font-size: 48rpx;
      color: #666;
    }
  }
}

.logout-area {
  margin-top: 60rpx;
  
  .btn-logout,
  .btn-login {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    font-size: 32rpx;
  }
}
</style>

