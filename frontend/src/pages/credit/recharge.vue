<template>
  <view class="container">
    <!-- 积分套餐 -->
    <view class="package-list">
      <view 
        v-for="pkg in packages" 
        :key="pkg.id"
        :class="['package-item', selectedPackage?.id === pkg.id ? 'selected' : '']"
        @click="selectPackage(pkg)"
      >
        <view class="package-info">
          <view class="package-name">{{ pkg.name }}</view>
          <view class="package-credits">
            {{ pkg.credits + pkg.bonusCredits }}积分
            <text v-if="pkg.bonusCredits > 0" class="bonus">
              (赠{{ pkg.bonusCredits }})
            </text>
          </view>
        </view>
        <view class="package-price">¥{{ pkg.price }}</view>
      </view>
    </view>

    <!-- 支付按钮 -->
    <button 
      class="btn-primary btn-pay"
      :disabled="!selectedPackage"
      @click="handlePay"
    >
      {{ selectedPackage ? `支付 ¥${selectedPackage.price}` : '请选择套餐' }}
    </button>

    <!-- 说明 -->
    <view class="tips">
      <text class="tips-title">充值说明</text>
      <text class="tips-item">• 积分用于生成视频，不同时长消耗不同</text>
      <text class="tips-item">• 充值后立即到账，永久有效</text>
      <text class="tips-item">• 支持微信支付，安全便捷</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const packages = ref([
  { id: 1, name: '体验套餐', credits: 100, bonusCredits: 0, price: 10 },
  { id: 2, name: '进阶套餐', credits: 300, bonusCredits: 20, price: 30 },
  { id: 3, name: '热门套餐', credits: 600, bonusCredits: 100, price: 50 },
  { id: 4, name: '超值套餐', credits: 1200, bonusCredits: 300, price: 100 }
])

const selectedPackage = ref(null)

const selectPackage = (pkg) => {
  selectedPackage.value = pkg
}

const handlePay = () => {
  if (!selectedPackage.value) {
    return
  }

  uni.showModal({
    title: '支付提示',
    content: '支付功能开发中，请稍后再试',
    showCancel: false
  })
  
  // TODO: 实现微信支付
}

onMounted(() => {
  // 默认选中第一个
  selectedPackage.value = packages.value[2]
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #0a0a0a;
  padding: 40rpx;
}

.package-list {
  margin-bottom: 60rpx;
  
  .package-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 40rpx 30rpx;
    background: #1a1a1a;
    border-radius: 20rpx;
    margin-bottom: 20rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s;
    
    &.selected {
      border-color: #00d9a3;
      background: rgba(0, 217, 163, 0.1);
    }
    
    .package-info {
      .package-name {
        font-size: 32rpx;
        color: #fff;
        font-weight: bold;
        margin-bottom: 10rpx;
      }
      
      .package-credits {
        font-size: 28rpx;
        color: #00d9a3;
        
        .bonus {
          color: #f59e0b;
          margin-left: 10rpx;
        }
      }
    }
    
    .package-price {
      font-size: 40rpx;
      font-weight: bold;
      color: #fff;
    }
  }
}

.btn-pay {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  margin-bottom: 60rpx;
}

.tips {
  padding: 40rpx;
  background: #1a1a1a;
  border-radius: 20rpx;
  
  .tips-title {
    display: block;
    font-size: 32rpx;
    color: #fff;
    font-weight: bold;
    margin-bottom: 30rpx;
  }
  
  .tips-item {
    display: block;
    font-size: 26rpx;
    color: #999;
    line-height: 2;
  }
}
</style>

