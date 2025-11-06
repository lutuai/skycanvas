<template>
  <view class="container">
    <!-- Tab切换 -->
    <view class="tabs">
      <view 
        :class="['tab-item', currentTab === 0 ? 'active' : '']"
        @click="handleTabChange(0)"
      >
        输入参数
      </view>
      <view 
        :class="['tab-item', currentTab === 1 ? 'active' : '']"
        @click="handleTabChange(1)"
      >
        历史记录
      </view>
    </view>

    <!-- 输入参数 -->
    <view v-show="currentTab === 0" class="tab-content">
      <!-- 图片上传 -->
      <view class="section">
        <view class="section-title">上传图片（图片不能有真人）</view>
        <view class="upload-area" @click="chooseImage">
          <image 
            v-if="formData.imageUrl" 
            :src="formData.imageUrl" 
            class="upload-image"
            mode="aspectFit"
          />
          <view v-else class="upload-placeholder">
            <text class="upload-icon">+</text>
            <text class="upload-text">点击上传图片</text>
          </view>
          <view v-if="formData.imageUrl" class="delete-btn" @click.stop="deleteImage">
            ×
          </view>
        </view>
      </view>

      <!-- 提示词 -->
      <view class="section">
        <view class="section-title">提示词</view>
        <textarea 
          v-model="formData.prompt"
          class="prompt-input"
          placeholder="描述你想要生成的视频内容..."
          maxlength="500"
        />
        <view class="input-counter">{{ formData.prompt.length }}/500</view>
      </view>

      <!-- 横竖屏选择 -->
      <view class="section">
        <view class="section-title">横竖屏选择（高清1920P）</view>
        <view class="option-item" @click="showResolutionPicker">
          <text>{{ resolutionText }}</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <!-- 生成时长 -->
      <view class="section">
        <view class="section-title">生成时长</view>
        <view class="option-item" @click="showDurationPicker">
          <text>{{ formData.duration }}秒</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <!-- 运行按钮 -->
      <button 
        class="btn-primary btn-submit"
        :loading="generating"
        :disabled="!canSubmit || generating"
        @click="handleGenerate"
      >
        {{ generating ? '生成中...' : '运行' }}
      </button>

      <!-- 提示信息 -->
      <view class="tips">
        <text class="tips-text">💡 本次生成需要消耗 {{ requiredCredits }} 积分</text>
      </view>
    </view>

    <!-- 历史记录 -->
    <view v-show="currentTab === 1" class="tab-content">
      <view class="history-list">
        <view 
          v-for="task in taskList" 
          :key="task.id"
          class="history-item"
          @click="handleTaskClick(task)"
        >
          <!-- 视频封面/预览图 -->
          <view class="task-preview">
            <image 
              v-if="task.coverUrl || task.videoUrl"
              :src="task.coverUrl || task.videoUrl"
              class="preview-image"
              mode="aspectFill"
            />
            <view v-else class="preview-placeholder">
              <text class="placeholder-icon">🎬</text>
            </view>
            
            <!-- 状态图标 -->
            <view v-if="task.status === 0 || task.status === 1" class="status-badge processing">
              <text class="badge-text">{{ task.status === 0 ? '排队中' : '生成中' }}</text>
              <text v-if="task.progress" class="badge-progress">{{ task.progress }}%</text>
            </view>
            <view v-else-if="task.status === 2" class="status-badge completed">
              <text class="badge-icon">▶</text>
            </view>
            <view v-else-if="task.status === 3" class="status-badge failed">
              <text class="badge-text">失败</text>
            </view>
          </view>
          
          <view class="task-info">
            <view class="task-prompt">{{ task.prompt }}</view>
            <view class="task-meta">
              <text class="task-status" :class="'status-' + task.status">
                {{ getStatusText(task.status) }}
              </text>
              <text class="task-time">{{ formatTime(task.createTime) }}</text>
            </view>
            <view v-if="task.duration" class="task-duration">
              <text>时长: {{ task.duration }}秒</text>
            </view>
          </view>
          
          <view class="task-credits">-{{ task.costCredits }}积分</view>
        </view>
        
        <view v-if="taskList.length === 0" class="empty">
          <text class="empty-text">暂无生成记录</text>
        </view>
      </view>
    </view>
    
    <!-- 视频播放模态框 -->
    <view v-if="showVideoModal" class="video-modal-overlay" @click="closeVideoModal">
      <view class="video-modal-container" @click.stop>
        <view class="video-modal-header">
          <text class="video-modal-title">视频预览</text>
          <text class="video-modal-close" @click="closeVideoModal">×</text>
        </view>
        <view class="video-modal-content">
          <video 
            v-if="currentVideo"
            :src="currentVideo.videoUrl"
            :poster="currentVideo.coverUrl"
            class="video-player"
            controls
            autoplay
          />
          <view class="video-info">
            <text class="video-prompt">{{ currentVideo?.prompt }}</text>
            <view class="video-meta">
              <text>时长: {{ currentVideo?.duration }}秒</text>
              <text>消耗: {{ currentVideo?.costCredits }}积分</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 尺寸选择器模态框 -->
    <view v-if="showResolutionModal" class="modal-overlay" @click="showResolutionModal = false">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">输入预览</text>
          <text class="modal-close" @click="showResolutionModal = false">×</text>
        </view>
        <view class="modal-content">
          <view 
            v-for="option in resolutionOptions" 
            :key="option.value"
            :class="['resolution-option', formData.aspectRatio === option.value ? 'selected' : '']"
            @click="selectResolution(option.value)"
          >
            <view class="option-text">{{ option.label }}</view>
            <view v-if="formData.aspectRatio === option.value" class="option-check">✓</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 时长选择器模态框 -->
    <view v-if="showDurationModal" class="modal-overlay" @click="showDurationModal = false">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">生成时长</text>
          <text class="modal-close" @click="showDurationModal = false">×</text>
        </view>
        <view class="modal-content">
          <view 
            v-for="option in durationOptions" 
            :key="option.value"
            :class="['resolution-option', formData.duration === option.value ? 'selected' : '']"
            @click="selectDuration(option.value)"
          >
            <view class="option-text">{{ option.label }}</view>
            <view v-if="formData.duration === option.value" class="option-check">✓</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { generateVideo, getMyTasks } from '@/api/video'
import { getBalance } from '@/api/credit'
import { showCustomModal } from '@/utils/modal'

const userStore = useUserStore()

// 当前Tab
const currentTab = ref(0)

// 表单数据
const formData = ref({
  imageUrl: '',
  prompt: '',
  resolution: '720p',
  aspectRatio: 'portrait_product',
  duration: 10
})

// 生成中状态
const generating = ref(false)

// 任务列表
const taskList = ref([])

// 自动刷新定时器
let refreshTimer = null

// 视频播放模态框
const showVideoModal = ref(false)
const currentVideo = ref(null)

// 显示尺寸选择器
const showResolutionModal = ref(false)

// 显示时长选择器
const showDurationModal = ref(false)

// 尺寸选项
const resolutionOptions = ref([
  { label: '竖屏—人像高清', value: 'portrait_person' },
  { label: '横屏—人像高清', value: 'landscape_person' },
  { label: '竖屏—商品高清', value: 'portrait_product' },
  { label: '横屏—商品高清', value: 'landscape_product' }
])

// 时长选项
const durationOptions = ref([
  { label: '10秒', value: 10 },
  { label: '15秒', value: 15 }
])

// 分辨率文本
const resolutionText = computed(() => {
  const option = resolutionOptions.value.find(o => o.value === formData.value.aspectRatio)
  return option ? option.label : '竖屏—商品高清'
})

// 所需积分
const requiredCredits = computed(() => {
  return formData.value.duration * 2 + (formData.value.resolution === '1080p' ? 5 : 0)
})

// 是否可以提交
const canSubmit = computed(() => {
  return formData.value.prompt.trim().length > 0
})

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      formData.value.imageUrl = res.tempFilePaths[0]
      // TODO: 上传到服务器
    }
  })
}

// 删除图片
const deleteImage = () => {
  formData.value.imageUrl = ''
}

// 显示分辨率选择器
const showResolutionPicker = () => {
  showResolutionModal.value = true
}

// 选择分辨率
const selectResolution = (value) => {
  formData.value.aspectRatio = value
  showResolutionModal.value = false
}

// 显示时长选择器
const showDurationPicker = () => {
  showDurationModal.value = true
}

// 选择时长
const selectDuration = (value) => {
  formData.value.duration = value
  showDurationModal.value = false
}

// 生成视频
const handleGenerate = async () => {
  if (!userStore.hasLogin) {
    await userStore.login()
    return
  }

  // 检查积分
  const balance = await getBalance()
  if (balance < requiredCredits.value) {
    const confirm = await showCustomModal({
      title: '积分不足',
      content: `当前积分：${balance}，需要：${requiredCredits.value}`,
      confirmText: '去充值'
    })
    
    if (confirm) {
      uni.navigateTo({
        url: '/pages/credit/recharge'
      })
    }
    return
  }

  generating.value = true

  try {
    const result = await generateVideo(formData.value)
    
    // 显示成功提示，引导用户查看历史记录
    await showCustomModal({
      title: '任务提交成功',
      content: '视频正在生成中，请到"历史记录"查看进度和结果',
      showCancel: false,
      confirmText: '去查看'
    })
    
    // 切换到历史记录
    currentTab.value = 1
    loadTaskList()
    
    // 清空表单
    formData.value = {
      imageUrl: '',
      prompt: '',
      resolution: '720p',
      aspectRatio: 'portrait_product',
      duration: 10
    }
  } catch (error) {
    console.error('生成失败:', error)
    uni.showToast({
      title: error.message || '提交失败',
      icon: 'none'
    })
  } finally {
    generating.value = false
  }
}

// 加载任务列表
const loadTaskList = async () => {
  try {
    const result = await getMyTasks({
      current: 1,
      size: 20
    })
    taskList.value = result.records || []
    
    // 如果有进行中的任务，启动自动刷新
    const hasProcessing = taskList.value.some(task => task.status === 0 || task.status === 1)
    if (hasProcessing && currentTab.value === 1) {
      startAutoRefresh()
    } else {
      stopAutoRefresh()
    }
  } catch (error) {
    console.error('加载任务列表失败:', error)
  }
}

// 启动自动刷新
const startAutoRefresh = () => {
  if (refreshTimer) return
  
  refreshTimer = setInterval(() => {
    if (currentTab.value === 1) {
      loadTaskList()
    }
  }, 5000) // 每5秒刷新一次
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 点击任务卡片
const handleTaskClick = async (task) => {
  if (task.status === 2 && task.videoUrl) {
    // 已完成且有视频，显示播放器
    currentVideo.value = task
    showVideoModal.value = true
  } else if (task.status === 3) {
    // 失败，显示错误信息
    await showCustomModal({
      title: '生成失败',
      content: task.errorMsg || '视频生成失败，积分已退回',
      showCancel: false
    })
  } else {
    // 进行中，显示进度
    uni.showToast({
      title: getStatusText(task.status) + (task.progress ? ` ${task.progress}%` : ''),
      icon: 'none'
    })
  }
}

// 关闭视频播放器
const closeVideoModal = () => {
  showVideoModal.value = false
  currentVideo.value = null
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    0: '排队中',
    1: '生成中',
    2: '已完成',
    3: '失败'
  }
  return map[status] || '未知'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  
  return date.toLocaleDateString()
}

// 监听tab切换
const handleTabChange = (tab) => {
  currentTab.value = tab
  if (tab === 1) {
    // 切换到历史记录，加载任务列表
    loadTaskList()
  } else {
    // 切换到输入参数，停止自动刷新
    stopAutoRefresh()
  }
}

onMounted(() => {
  if (userStore.hasLogin) {
    loadTaskList()
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #0a0a0a;
}

.tabs {
  display: flex;
  background: #1a1a1a;
  border-bottom: 2rpx solid #333;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 30rpx 0;
    font-size: 32rpx;
    color: #999;
    position: relative;
    
    &.active {
      color: #00d9a3;
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: #00d9a3;
        border-radius: 2rpx;
      }
    }
  }
}

.tab-content {
  padding: 40rpx;
}

.section {
  margin-bottom: 40rpx;
  
  .section-title {
    font-size: 28rpx;
    color: #fff;
    margin-bottom: 20rpx;
  }
}

.upload-area {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  background: #2a2a2a;
  border: 2rpx dashed #666;
  border-radius: 16rpx;
  overflow: hidden;
  
  .upload-image {
    width: 100%;
    height: 100%;
  }
  
  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    
    .upload-icon {
      font-size: 60rpx;
      color: #666;
    }
    
    .upload-text {
      font-size: 24rpx;
      color: #999;
      margin-top: 10rpx;
    }
  }
  
  .delete-btn {
    position: absolute;
    top: -10rpx;
    right: -10rpx;
    width: 40rpx;
    height: 40rpx;
    background: rgba(255, 0, 0, 0.8);
    border-radius: 50%;
    color: #fff;
    font-size: 36rpx;
    line-height: 40rpx;
    text-align: center;
  }
}

.prompt-input {
  width: 100%;
  min-height: 200rpx;
  background: #2a2a2a;
  border: 2rpx solid #333;
  border-radius: 16rpx;
  padding: 20rpx;
  color: #fff;
  font-size: 28rpx;
}

.input-counter {
  text-align: right;
  font-size: 24rpx;
  color: #666;
  margin-top: 10rpx;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background: #2a2a2a;
  border-radius: 16rpx;
  color: #fff;
  font-size: 28rpx;
  
  .arrow {
    font-size: 40rpx;
    color: #666;
  }
}

.btn-submit {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  margin-top: 60rpx;
  font-size: 32rpx;
}

.tips {
  text-align: center;
  margin-top: 30rpx;
  
  .tips-text {
    font-size: 24rpx;
    color: #999;
  }
}

.history-list {
  .history-item {
    display: flex;
    gap: 20rpx;
    padding: 20rpx;
    background: #1a1a1a;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    transition: all 0.3s ease;
    
    &:active {
      opacity: 0.8;
      transform: scale(0.98);
    }
    
    .task-preview {
      position: relative;
      width: 160rpx;
      height: 160rpx;
      flex-shrink: 0;
      border-radius: 12rpx;
      overflow: hidden;
      background: #2a2a2a;
      
      .preview-image {
        width: 100%;
        height: 100%;
      }
      
      .preview-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .placeholder-icon {
          font-size: 60rpx;
        }
      }
      
      .status-badge {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 8rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8rpx;
        font-size: 20rpx;
        backdrop-filter: blur(10rpx);
        
        &.processing {
          background: rgba(59, 130, 246, 0.9);
          color: #fff;
        }
        
        &.completed {
          background: rgba(16, 185, 129, 0.9);
          color: #fff;
          
          .badge-icon {
            font-size: 24rpx;
          }
        }
        
        &.failed {
          background: rgba(239, 68, 68, 0.9);
          color: #fff;
        }
        
        .badge-text {
          font-weight: bold;
        }
        
        .badge-progress {
          font-weight: bold;
        }
      }
    }
    
    .task-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      min-width: 0;
      
      .task-prompt {
        font-size: 28rpx;
        color: #fff;
        margin-bottom: 10rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .task-meta {
        display: flex;
        gap: 20rpx;
        font-size: 24rpx;
        
        .task-status {
          &.status-0 {
            color: #f59e0b;
          }
          &.status-1 {
            color: #3b82f6;
          }
          &.status-2 {
            color: #10b981;
          }
          &.status-3 {
            color: #ef4444;
          }
        }
        
        .task-time {
          color: #999;
        }
      }
      
      .task-duration {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
    
    .task-credits {
      color: #00d9a3;
      font-size: 28rpx;
      font-weight: bold;
      white-space: nowrap;
      align-self: flex-start;
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
}

// 视频播放模态框
.video-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.video-modal-container {
  width: 90%;
  max-width: 800rpx;
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

.video-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx;
  border-bottom: 2rpx solid #333;
  
  .video-modal-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #fff;
  }
  
  .video-modal-close {
    font-size: 60rpx;
    color: #999;
    line-height: 40rpx;
    cursor: pointer;
    
    &:active {
      opacity: 0.7;
    }
  }
}

.video-modal-content {
  padding: 20rpx;
  
  .video-player {
    width: 100%;
    height: 400rpx;
    border-radius: 16rpx;
    background: #000;
  }
  
  .video-info {
    padding: 30rpx 20rpx;
    
    .video-prompt {
      font-size: 28rpx;
      color: #fff;
      line-height: 1.6;
      margin-bottom: 20rpx;
      display: block;
    }
    
    .video-meta {
      display: flex;
      gap: 30rpx;
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 模态框样式
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
  z-index: 1000;
  padding: 40rpx;
}

.modal-container {
  width: 100%;
  max-width: 600rpx;
  background: #1a1a1a;
  border-radius: 24rpx;
  overflow: hidden;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  border-bottom: 2rpx solid #333;
  
  .modal-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #fff;
  }
  
  .modal-close {
    font-size: 60rpx;
    color: #999;
    line-height: 40rpx;
    cursor: pointer;
    
    &:active {
      opacity: 0.7;
    }
  }
}

.modal-content {
  padding: 20rpx;
}

.resolution-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 30rpx;
  margin: 10rpx 0;
  background: #2a2a2a;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  
  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
  
  &.selected {
    background: linear-gradient(135deg, rgba(0, 217, 163, 0.15), rgba(0, 217, 163, 0.05));
    border-color: #00d9a3;
    
    .option-text {
      color: #00d9a3;
      font-weight: bold;
    }
  }
  
  .option-text {
    font-size: 30rpx;
    color: #fff;
    transition: color 0.3s ease;
  }
  
  .option-check {
    font-size: 32rpx;
    color: #00d9a3;
    font-weight: bold;
  }
}
</style>

