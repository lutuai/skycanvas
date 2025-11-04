<template>
  <view class="container">
    <view v-if="works.length > 0" class="work-list">
      <view 
        v-for="work in works" 
        :key="work.id"
        class="work-item"
      >
        <image :src="work.coverUrl" class="work-cover" mode="aspectFill" />
        <view class="work-info">
          <view class="work-title">{{ work.title || '未命名作品' }}</view>
          <view class="work-meta">
            <text class="work-duration">{{ work.duration }}秒</text>
            <text class="work-time">{{ formatTime(work.createTime) }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <view v-else class="empty">
      <text class="empty-icon">🎬</text>
      <text class="empty-text">还没有作品</text>
      <button class="btn-primary btn-create" @click="goCreate">
        开始创作
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const works = ref([])

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString()
}

const goCreate = () => {
  uni.switchTab({
    url: '/pages/generate/index'
  })
}

onMounted(() => {
  // TODO: 加载作品列表
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #0a0a0a;
  padding: 40rpx;
}

.work-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  
  .work-item {
    width: calc(50% - 10rpx);
    background: #1a1a1a;
    border-radius: 16rpx;
    overflow: hidden;
    
    .work-cover {
      width: 100%;
      height: 300rpx;
    }
    
    .work-info {
      padding: 20rpx;
      
      .work-title {
        font-size: 28rpx;
        color: #fff;
        margin-bottom: 10rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .work-meta {
        display: flex;
        justify-content: space-between;
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 40rpx;
  
  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 40rpx;
  }
  
  .empty-text {
    font-size: 32rpx;
    color: #666;
    margin-bottom: 60rpx;
  }
  
  .btn-create {
    width: 400rpx;
    height: 88rpx;
    line-height: 88rpx;
    border-radius: 44rpx;
    font-size: 32rpx;
  }
}
</style>

