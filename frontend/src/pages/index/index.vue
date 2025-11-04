<template>
  <view class="container">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-title">SkyCanvas</view>
      <view class="nav-subtitle">AI视频创作平台</view>
    </view>

    <!-- Banner -->
    <view class="banner">
      <view class="banner-content">
        <text class="banner-title">让想象成为现实</text>
        <text class="banner-desc">输入文字，AI为你生成精彩视频</text>
        <button class="btn-primary btn-start" @click="goGenerate">
          立即开始创作
        </button>
      </view>
    </view>

    <!-- 功能介绍 -->
    <view class="features">
      <view class="section-title">平台特色</view>
      <view class="feature-list">
        <view class="feature-item">
          <view class="feature-icon">🎬</view>
          <view class="feature-name">AI生成</view>
          <view class="feature-desc">先进的Sora技术</view>
        </view>
        <view class="feature-item">
          <view class="feature-icon">⚡</view>
          <view class="feature-name">快速生成</view>
          <view class="feature-desc">几秒钟即可完成</view>
        </view>
        <view class="feature-item">
          <view class="feature-icon">🎨</view>
          <view class="feature-name">多种风格</view>
          <view class="feature-desc">满足各种需求</view>
        </view>
        <view class="feature-item">
          <view class="feature-icon">📱</view>
          <view class="feature-name">多端使用</view>
          <view class="feature-desc">小程序/H5/Web</view>
        </view>
      </view>
    </view>

    <!-- 热门作品 -->
    <view class="hot-works">
      <view class="section-title">热门作品</view>
      <view class="work-list">
        <view class="work-placeholder">
          <text class="placeholder-text">敬请期待...</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 跳转到生成页
const goGenerate = () => {
  if (!userStore.hasLogin) {
    userStore.login().then(() => {
      uni.switchTab({
        url: '/pages/generate/index'
      })
    })
  } else {
    uni.switchTab({
      url: '/pages/generate/index'
    })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #0a0a0a 0%, #1a1a1a 100%);
}

.custom-nav {
  padding: 100rpx 40rpx 60rpx;
  text-align: center;
  
  .nav-title {
    font-size: 60rpx;
    font-weight: bold;
    background: linear-gradient(135deg, #4ade80 0%, #00d9a3 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .nav-subtitle {
    font-size: 28rpx;
    color: #999;
    margin-top: 10rpx;
  }
}

.banner {
  padding: 80rpx 40rpx;
  text-align: center;
  
  .banner-content {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .banner-title {
    font-size: 56rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 20rpx;
  }
  
  .banner-desc {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 60rpx;
  }
  
  .btn-start {
    width: 400rpx;
    height: 88rpx;
    line-height: 88rpx;
    border-radius: 44rpx;
    font-size: 32rpx;
  }
}

.features,
.hot-works {
  padding: 60rpx 40rpx;
}

.section-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 40rpx;
}

.feature-list {
  display: flex;
  flex-wrap: wrap;
  gap: 30rpx;
}

.feature-item {
  width: calc(50% - 15rpx);
  background: #1a1a1a;
  border-radius: 20rpx;
  padding: 40rpx;
  text-align: center;
  
  .feature-icon {
    font-size: 60rpx;
    margin-bottom: 20rpx;
  }
  
  .feature-name {
    font-size: 32rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 10rpx;
  }
  
  .feature-desc {
    font-size: 24rpx;
    color: #999;
  }
}

.work-placeholder {
  background: #1a1a1a;
  border-radius: 20rpx;
  padding: 100rpx;
  text-align: center;
  
  .placeholder-text {
    font-size: 28rpx;
    color: #666;
  }
}
</style>

