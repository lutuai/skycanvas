<template>
  <view class="container">
    <!-- 积分余额卡片 -->
    <view class="balance-card">
      <view class="balance-label">当前积分</view>
      <view class="balance-value">{{ balance }}</view>
    </view>

    <!-- 积分记录列表 -->
    <view class="log-list">
      <view 
        v-for="log in logs" 
        :key="log.id"
        class="log-item"
      >
        <view class="log-info">
          <view class="log-desc">{{ log.description }}</view>
          <view class="log-time">{{ formatTime(log.createTime) }}</view>
        </view>
        <view :class="['log-amount', log.amount > 0 ? 'income' : 'expense']">
          {{ log.amount > 0 ? '+' : '' }}{{ log.amount }}
        </view>
      </view>
      
      <view v-if="logs.length === 0" class="empty">
        <text class="empty-text">暂无积分记录</text>
      </view>
      
      <view v-if="hasMore" class="load-more" @click="loadMore">
        <text>加载更多</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCreditLogs, getBalance } from '@/api/credit'

const balance = ref(0)
const logs = ref([])
const current = ref(1)
const hasMore = ref(true)

// 加载积分余额
const loadBalance = async () => {
  try {
    balance.value = await getBalance()
  } catch (error) {
    console.error('加载积分余额失败:', error)
  }
}

// 加载积分记录
const loadLogs = async () => {
  try {
    const result = await getCreditLogs({
      current: current.value,
      size: 20
    })
    
    if (current.value === 1) {
      logs.value = result.records || []
    } else {
      logs.value = [...logs.value, ...(result.records || [])]
    }
    
    hasMore.value = current.value < result.pages
  } catch (error) {
    console.error('加载积分记录失败:', error)
  }
}

// 加载更多
const loadMore = () => {
  if (hasMore.value) {
    current.value++
    loadLogs()
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hour}:${minute}`
}

onMounted(() => {
  loadBalance()
  loadLogs()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #0a0a0a;
  padding: 40rpx;
}

.balance-card {
  background: linear-gradient(135deg, #4ade80 0%, #00d9a3 100%);
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  margin-bottom: 40rpx;
  
  .balance-label {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 20rpx;
  }
  
  .balance-value {
    font-size: 72rpx;
    font-weight: bold;
    color: #fff;
  }
}

.log-list {
  .log-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    background: #1a1a1a;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    
    .log-info {
      flex: 1;
      
      .log-desc {
        font-size: 28rpx;
        color: #fff;
        margin-bottom: 10rpx;
      }
      
      .log-time {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .log-amount {
      font-size: 32rpx;
      font-weight: bold;
      
      &.income {
        color: #10b981;
      }
      
      &.expense {
        color: #ef4444;
      }
    }
  }
  
  .empty {
    padding: 100rpx;
    text-align: center;
    
    .empty-text {
      font-size: 28rpx;
      color: #666;
    }
  }
  
  .load-more {
    padding: 30rpx;
    text-align: center;
    font-size: 28rpx;
    color: #00d9a3;
  }
}
</style>

