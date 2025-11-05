<template>
  <view class="container">
    <view class="header">
      <text class="title">头像预览</text>
      <text class="subtitle">查看不同用户ID生成的默认头像效果</text>
    </view>

    <!-- 类型切换 -->
    <view class="type-switcher">
      <view 
        class="type-btn"
        :class="{ active: avatarType === 'svg' }"
        @click="avatarType = 'svg'"
      >
        SVG渐变
      </view>
      <view 
        class="type-btn"
        :class="{ active: avatarType === 'dicebear' }"
        @click="avatarType = 'dicebear'"
      >
        DiceBear API
      </view>
    </view>

    <!-- 风格选择（仅DiceBear） -->
    <view v-if="avatarType === 'dicebear'" class="style-selector">
      <text class="label">选择风格：</text>
      <picker 
        :value="styleIndex" 
        :range="styles" 
        range-key="label"
        @change="onStyleChange"
      >
        <view class="picker-value">{{ styles[styleIndex].label }}</view>
      </picker>
    </view>

    <!-- 头像网格 -->
    <view class="avatars-grid">
      <view 
        v-for="id in sampleIds" 
        :key="id"
        class="avatar-item"
      >
        <image 
          :src="getAvatarUrl(id)" 
          class="avatar"
          mode="aspectFill"
        />
        <text class="avatar-id">ID: {{ id }}</text>
      </view>
    </view>

    <!-- 随机生成按钮 -->
    <button class="btn-primary btn-regenerate" @click="regenerateIds">
      重新生成示例
    </button>

    <!-- 当前用户头像 -->
    <view v-if="userStore.hasLogin" class="current-user">
      <view class="section-title">你的头像</view>
      <view class="user-avatar-card">
        <image 
          :src="getUserAvatar(userStore.userInfo, avatarType)" 
          class="user-avatar"
        />
        <view class="user-info">
          <text class="user-nickname">{{ userStore.nickname }}</text>
          <text class="user-id">ID: {{ userStore.userInfo?.id || '未知' }}</text>
          <text class="user-emoji">{{ getEmojiByUserId(userStore.userInfo?.id) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { 
  getUserAvatar, 
  getDefaultAvatar,
  generateSvgAvatar,
  getAvatarStyles,
  getEmojiByUserId
} from '@/utils/avatar'

const userStore = useUserStore()

// 头像类型
const avatarType = ref('svg')

// DiceBear风格
const styles = getAvatarStyles()
const styleIndex = ref(3) // 默认 fun-emoji

// 示例ID列表
const sampleIds = ref([])

// 生成随机ID
const generateRandomIds = () => {
  const ids = []
  for (let i = 0; i < 12; i++) {
    ids.push(Math.floor(Math.random() * 10000))
  }
  return ids
}

// 初始化示例ID
sampleIds.value = generateRandomIds()

// 风格切换
const onStyleChange = (e) => {
  styleIndex.value = e.detail.value
}

// 获取头像URL
const getAvatarUrl = (id) => {
  if (avatarType.value === 'svg') {
    return generateSvgAvatar(id)
  } else {
    const style = styles[styleIndex.value].value
    return getDefaultAvatar(id, style)
  }
}

// 重新生成示例
const regenerateIds = () => {
  sampleIds.value = generateRandomIds()
  uni.showToast({
    title: '已生成新的示例',
    icon: 'success'
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #0a0a0a;
  padding: 40rpx;
}

.header {
  margin-bottom: 40rpx;
  text-align: center;
  
  .title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 20rpx;
  }
  
  .subtitle {
    display: block;
    font-size: 26rpx;
    color: #999;
  }
}

.type-switcher {
  display: flex;
  gap: 20rpx;
  margin-bottom: 40rpx;
  background: #1a1a1a;
  border-radius: 16rpx;
  padding: 8rpx;
  
  .type-btn {
    flex: 1;
    padding: 20rpx;
    text-align: center;
    font-size: 28rpx;
    color: #999;
    border-radius: 12rpx;
    transition: all 0.3s;
    
    &.active {
      background: linear-gradient(135deg, #00d9a3 0%, #00b386 100%);
      color: #fff;
      font-weight: bold;
    }
  }
}

.style-selector {
  background: #1a1a1a;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  
  .label {
    display: block;
    font-size: 28rpx;
    color: #999;
    margin-bottom: 20rpx;
  }
  
  .picker-value {
    padding: 20rpx;
    background: #2a2a2a;
    border-radius: 12rpx;
    color: #00d9a3;
    font-size: 28rpx;
    text-align: center;
  }
}

.avatars-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30rpx;
  margin-bottom: 40rpx;
  
  .avatar-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx;
    background: #1a1a1a;
    border-radius: 20rpx;
    
    .avatar {
      width: 160rpx;
      height: 160rpx;
      border-radius: 80rpx;
      margin-bottom: 20rpx;
      background: #2a2a2a;
    }
    
    .avatar-id {
      font-size: 22rpx;
      color: #666;
    }
  }
}

.btn-regenerate {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  margin-bottom: 60rpx;
}

.current-user {
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 30rpx;
    text-align: center;
  }
  
  .user-avatar-card {
    display: flex;
    align-items: center;
    gap: 40rpx;
    padding: 40rpx;
    background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
    border-radius: 24rpx;
    border: 2rpx solid rgba(0, 217, 163, 0.3);
    
    .user-avatar {
      width: 160rpx;
      height: 160rpx;
      border-radius: 80rpx;
      background: #2a2a2a;
    }
    
    .user-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 15rpx;
      
      .user-nickname {
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .user-id {
        font-size: 24rpx;
        color: #999;
      }
      
      .user-emoji {
        font-size: 48rpx;
      }
    }
  }
}
</style>

