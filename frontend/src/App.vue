<template>
  <!-- 全局自定义深色弹窗组件 -->
  <CustomModal />
</template>

<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import CustomModal from '@/components/CustomModal.vue'

onLaunch(() => {
  console.log('App Launch')
  // 自动登录
  const userStore = useUserStore()
  userStore.initLogin()
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
/* 全局样式 */
page {
  background-color: #0a0a0a;
  color: #ffffff;
}

/* 自定义主题色 */
:root {
  --primary-color: #00d9a3;
  --primary-gradient: linear-gradient(135deg, #4ade80 0%, #00d9a3 100%);
  --bg-dark: #0a0a0a;
  --bg-card: #1a1a1a;
  --bg-input: #2a2a2a;
  --text-primary: #ffffff;
  --text-secondary: #999999;
  --border-color: #333333;
}

/* 通用卡片样式 */
.card {
  background: var(--bg-card);
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

/* 通用按钮样式 */
.btn-primary {
  background: var(--primary-gradient);
  color: #ffffff;
  border-radius: 50rpx;
  font-weight: bold;
}

.btn-ghost {
  background: transparent;
  border: 2rpx solid var(--primary-color);
  color: var(--primary-color);
  border-radius: 50rpx;
}

/* 输入框样式 */
.input-dark {
  background: var(--bg-input);
  border: 2rpx solid var(--border-color);
  border-radius: 12rpx;
  color: var(--text-primary);
  padding: 20rpx;
}
</style>

