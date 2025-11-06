<template>
  <view v-if="visible" class="modal-overlay" @tap="handleOverlayClick">
    <view class="modal-container" @tap.stop>
      <!-- 标题 -->
      <view v-if="title" class="modal-header">
        <text class="modal-title">{{ title }}</text>
      </view>
      
      <!-- 内容 -->
      <view class="modal-content">
        <text class="modal-text">{{ content }}</text>
      </view>
      
      <!-- 按钮 -->
      <view class="modal-footer">
        <view 
          v-if="showCancel" 
          class="modal-button cancel-button"
          hover-class="button-hover"
          @tap="handleCancel"
        >
          <text class="button-text cancel-text">{{ cancelText }}</text>
        </view>
        <view 
          class="modal-button confirm-button"
          :class="{ 'full-width': !showCancel }"
          hover-class="button-hover"
          @tap="handleConfirm"
        >
          <text class="button-text confirm-text">{{ confirmText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { setModalInstance } from '@/utils/modal'

const visible = ref(false)
const title = ref('提示')
const content = ref('')
const confirmText = ref('确定')
const cancelText = ref('取消')
const showCancel = ref(true)
const closeOnClickOverlay = ref(true)

let resolveCallback = null

// 立即注册实例（不等待 onMounted）
setModalInstance({
  show: showModal
})

onMounted(() => {
  console.log('CustomModal 组件已挂载')
})

/**
 * 显示弹窗
 */
function showModal(options = {}) {
  return new Promise((resolve) => {
    title.value = options.title || '提示'
    content.value = options.content || ''
    confirmText.value = options.confirmText || '确定'
    cancelText.value = options.cancelText || '取消'
    showCancel.value = options.showCancel !== false
    closeOnClickOverlay.value = options.closeOnClickOverlay !== false
    
    visible.value = true
    resolveCallback = resolve
  })
}

/**
 * 点击确认
 */
function handleConfirm() {
  visible.value = false
  if (resolveCallback) {
    resolveCallback(true)
    resolveCallback = null
  }
}

/**
 * 点击取消
 */
function handleCancel() {
  visible.value = false
  if (resolveCallback) {
    resolveCallback(false)
    resolveCallback = null
  }
}

/**
 * 点击遮罩层
 */
function handleOverlayClick() {
  if (closeOnClickOverlay.value) {
    handleCancel()
  }
}
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-container {
  width: 560rpx;
  background: #1a1a1a;
  border-radius: 24rpx;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(100rpx);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 40rpx 40rpx 20rpx;
  text-align: center;
  border-bottom: 2rpx solid #333;
  
  .modal-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #fff;
    line-height: 1.4;
  }
}

.modal-content {
  padding: 40rpx;
  min-height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .modal-text {
    font-size: 28rpx;
    color: #ccc;
    line-height: 1.6;
    text-align: center;
    word-break: break-all;
  }
}

.modal-footer {
  display: flex;
  border-top: 2rpx solid #333;
  
  .modal-button {
    flex: 1;
    height: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;
    
    &.full-width {
      flex: 1;
    }
    
    .button-text {
      font-size: 32rpx;
    }
    
    &.cancel-button {
      border-right: 2rpx solid #333;
      
      .cancel-text {
        color: #999;
      }
    }
    
    &.confirm-button {
      .confirm-text {
        color: #00d9a3;
        font-weight: bold;
      }
    }
    
    &.button-hover {
      background-color: rgba(255, 255, 255, 0.05);
    }
  }
}
</style>

