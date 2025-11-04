<template>
  <view class="container">
    <view class="placeholder">
      <text class="placeholder-icon">🚀</text>
      <text class="placeholder-text">社区功能即将上线</text>
      <text class="placeholder-desc">在这里分享你的精彩作品</text>
    </view>
  </view>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #0a0a0a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx;
  
  .placeholder-icon {
    font-size: 120rpx;
    margin-bottom: 40rpx;
  }
  
  .placeholder-text {
    font-size: 36rpx;
    color: #fff;
    font-weight: bold;
    margin-bottom: 20rpx;
  }
  
  .placeholder-desc {
    font-size: 28rpx;
    color: #999;
  }
}
</style>

