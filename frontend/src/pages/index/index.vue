<template>
  <view class="container">
    <!-- Hero Section -->
    <view class="hero-section">
      <view class="hero-content">
        <view class="logo-area">
          <text class="logo-text">SkyCanvas</text>
          <view class="logo-tagline">AI 视频创作平台</view>
        </view>
        
        <view class="hero-main">
          <text class="hero-title">让想象成为现实</text>
          <text class="hero-subtitle">只需一句话，AI 即刻为你生成精彩视频</text>
          <button class="cta-button" @click="goGenerate">
            <text class="cta-text">开始创作</text>
            <text class="cta-icon">→</text>
          </button>
        </view>
      </view>
    </view>

    <!-- Features Grid -->
    <view class="features-section">
      <view class="section-header">
        <text class="section-title">核心特性</text>
        <view class="title-line"></view>
      </view>
      
      <view class="features-grid">
        <view class="feature-card" v-for="(feature, index) in features" :key="index">
          <view class="feature-icon-wrapper">
            <text class="feature-icon">{{ feature.icon }}</text>
          </view>
          <view class="feature-content">
            <text class="feature-title">{{ feature.title }}</text>
            <text class="feature-desc">{{ feature.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Hot Works Section -->
    <view class="works-section">
      <view class="section-header">
        <text class="section-title">热门作品</text>
        <view class="title-line"></view>
      </view>
      
      <view class="works-placeholder">
        <text class="placeholder-icon">🎬</text>
        <text class="placeholder-title">精彩内容即将上线</text>
        <text class="placeholder-desc">敬请期待更多创作者的优秀作品</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 功能特性数据
const features = ref([
  {
    icon: '🎬',
    title: 'AI 智能生成',
    desc: '基于先进的 Sora 技术，智能理解你的创意'
  },
  {
    icon: '⚡',
    title: '极速处理',
    desc: '强大的云端算力，秒级完成视频生成'
  },
  {
    icon: '🎨',
    title: '风格多样',
    desc: '多种艺术风格可选，满足不同创作需求'
  },
  {
    icon: '📱',
    title: '全平台支持',
    desc: '微信小程序、H5、Web 多端无缝使用'
  }
])

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
// ==================== 基础容器 ====================
.container {
  min-height: 100vh;
  background: #0a0a0a;
  position: relative;
  padding-bottom: 120rpx;
  
  // 背景渐变效果
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 800rpx;
    background: radial-gradient(ellipse at top, rgba(0, 217, 163, 0.15), transparent 70%);
    pointer-events: none;
    z-index: 0;
  }
}

// ==================== Hero 区域 ====================
.hero-section {
  padding: 60rpx 32rpx 80rpx;
  position: relative;
  z-index: 1;
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 80rpx;
}

.logo-area {
  text-align: center;
  
  .logo-text {
    display: block;
    font-size: 56rpx;
    font-weight: 800;
    background: linear-gradient(135deg, #4ade80, #00d9a3, #00bfa5);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 4rpx;
    margin-bottom: 12rpx;
  }
  
  .logo-tagline {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.5);
    letter-spacing: 6rpx;
    text-transform: uppercase;
  }
}

.hero-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 60rpx 24rpx;
  background: linear-gradient(135deg, 
    rgba(26, 26, 26, 0.4) 0%, 
    rgba(20, 20, 20, 0.3) 100%
  );
  border-radius: 28rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20rpx);
  box-shadow: 
    0 8rpx 32rpx rgba(0, 0, 0, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.05);
  
  .hero-title {
    font-size: 52rpx;
    font-weight: 800;
    color: #ffffff;
    line-height: 1.3;
    margin-bottom: 20rpx;
    letter-spacing: 1rpx;
  }
  
  .hero-subtitle {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.65);
    line-height: 1.6;
    margin-bottom: 48rpx;
    max-width: 520rpx;
  }
}

.cta-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 0 48rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #4ade80 0%, #00d9a3 100%);
  border-radius: 44rpx;
  border: none;
  box-shadow: 
    0 4rpx 16rpx rgba(0, 217, 163, 0.4),
    0 8rpx 32rpx rgba(0, 217, 163, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: translateY(2rpx) scale(0.98);
    box-shadow: 
      0 2rpx 8rpx rgba(0, 217, 163, 0.3),
      0 4rpx 16rpx rgba(0, 217, 163, 0.15);
  }
  
  .cta-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #ffffff;
    letter-spacing: 1rpx;
  }
  
  .cta-icon {
    font-size: 32rpx;
    color: #ffffff;
    transition: transform 0.3s ease;
  }
  
  &:active .cta-icon {
    transform: translateX(4rpx);
  }
}

// ==================== 功能区域 ====================
.features-section {
  padding: 60rpx 32rpx;
  position: relative;
  z-index: 1;
}

.section-header {
  margin-bottom: 32rpx;
  
  .section-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #ffffff;
    margin-bottom: 12rpx;
  }
  
  .title-line {
    width: 60rpx;
    height: 4rpx;
    background: linear-gradient(90deg, #4ade80, #00d9a3);
    border-radius: 2rpx;
  }
}

.features-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.feature-card {
  display: flex;
  flex-direction: column;
  padding: 32rpx 24rpx;
  background: linear-gradient(135deg, 
    rgba(26, 26, 26, 0.6) 0%, 
    rgba(20, 20, 20, 0.4) 100%
  );
  border-radius: 20rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.06);
  transition: all 0.3s ease;
  
  &:active {
    transform: translateY(-4rpx);
    border-color: rgba(0, 217, 163, 0.3);
    background: linear-gradient(135deg, 
      rgba(26, 26, 26, 0.8) 0%, 
      rgba(20, 20, 20, 0.6) 100%
    );
    box-shadow: 0 8rpx 24rpx rgba(0, 217, 163, 0.15);
  }
  
  .feature-icon-wrapper {
    margin-bottom: 16rpx;
    
    .feature-icon {
      font-size: 56rpx;
      line-height: 1;
    }
  }
  
  .feature-content {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    
    .feature-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #ffffff;
      line-height: 1.3;
    }
    
    .feature-desc {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.5);
      line-height: 1.5;
    }
  }
}

// ==================== 作品区域 ====================
.works-section {
  padding: 60rpx 32rpx 80rpx;
  position: relative;
  z-index: 1;
}

.works-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
  background: linear-gradient(135deg, 
    rgba(26, 26, 26, 0.3) 0%, 
    rgba(20, 20, 20, 0.2) 100%
  );
  border-radius: 24rpx;
  border: 2rpx dashed rgba(255, 255, 255, 0.08);
  
  .placeholder-icon {
    font-size: 72rpx;
    margin-bottom: 20rpx;
    opacity: 0.3;
  }
  
  .placeholder-title {
    font-size: 28rpx;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.4);
    margin-bottom: 12rpx;
  }
  
  .placeholder-desc {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.25);
    line-height: 1.5;
  }
}
</style>

