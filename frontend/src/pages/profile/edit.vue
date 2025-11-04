<template>
  <view class="container">
    <view class="edit-card">
      <!-- 头像 -->
      <view class="form-item">
        <view class="label">头像</view>
        <view class="avatar-uploader" @click="chooseAvatar">
          <image :src="form.avatar || '/static/default-avatar.png'" class="avatar-preview" />
          <view class="upload-btn">点击更换</view>
        </view>
      </view>

      <!-- 昵称 -->
      <view class="form-item">
        <view class="label">昵称</view>
        <input 
          v-model="form.nickname" 
          class="input-dark" 
          placeholder="请输入昵称" 
          maxlength="50"
        />
      </view>

      <!-- 手机号 -->
      <view class="form-item">
        <view class="label">手机号</view>
        <view v-if="userInfo?.phone" class="phone-display">
          <text>{{ userInfo.phone }}</text>
          <text class="verified-badge">已绑定</text>
        </view>
        <view v-else class="phone-bind" @click="showPhoneModal = true">
          <text class="bind-text">点击绑定手机号</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <!-- 保存按钮 -->
      <button 
        class="btn-primary btn-save" 
        @click="handleSave"
        :loading="saving"
        :disabled="saving"
      >
        {{ saving ? '保存中...' : '保存修改' }}
      </button>
    </view>

    <!-- 手机号绑定弹窗 -->
    <uni-popup ref="phonePopup" type="center" :mask-click="false">
      <view class="phone-modal">
        <view class="modal-title">绑定手机号</view>
        
        <view class="modal-form">
          <input 
            v-model="phoneForm.phone" 
            class="input-dark" 
            placeholder="请输入手机号" 
            type="number"
            maxlength="11"
          />
          
          <view class="code-input-wrapper">
            <input 
              v-model="phoneForm.code" 
              class="input-dark code-input" 
              placeholder="请输入验证码" 
              type="number"
              maxlength="6"
            />
            <button 
              class="btn-ghost btn-code" 
              @click="sendCode"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </button>
          </view>
        </view>

        <view class="modal-actions">
          <button class="btn-ghost btn-cancel" @click="closePhoneModal">取消</button>
          <button 
            class="btn-primary btn-confirm" 
            @click="handleBindPhone"
            :loading="binding"
            :disabled="binding"
          >
            确定
          </button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 表单数据
const form = reactive({
  nickname: userInfo.value?.nickname || '',
  avatar: userInfo.value?.avatar || ''
})

// 手机号绑定表单
const phoneForm = reactive({
  phone: '',
  code: ''
})

const saving = ref(false)
const binding = ref(false)
const countdown = ref(0)
const showPhoneModal = ref(false)
const phonePopup = ref(null)

// 选择头像
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // TODO: 上传图片到服务器
      const tempFilePath = res.tempFilePaths[0]
      form.avatar = tempFilePath
      
      uni.showToast({
        title: '头像已选择，请保存',
        icon: 'none'
      })
    }
  })
}

// 保存用户信息
const handleSave = async () => {
  if (!form.nickname || !form.nickname.trim()) {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    })
    return
  }

  saving.value = true
  try {
    await userStore.updateUserInfo({
      nickname: form.nickname.trim(),
      avatar: form.avatar
    })
    
    // 返回上一页
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

// 发送验证码
const sendCode = async () => {
  if (!phoneForm.phone || phoneForm.phone.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    uni.showToast({
      title: '手机号格式不正确',
      icon: 'none'
    })
    return
  }

  try {
    await userStore.sendSmsCode(phoneForm.phone)
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

// 绑定手机号
const handleBindPhone = async () => {
  if (!phoneForm.phone || phoneForm.phone.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  if (!phoneForm.code || phoneForm.code.length !== 6) {
    uni.showToast({
      title: '请输入6位验证码',
      icon: 'none'
    })
    return
  }

  binding.value = true
  try {
    await userStore.bindPhone({
      phone: phoneForm.phone,
      code: phoneForm.code
    })
    
    closePhoneModal()
  } catch (error) {
    console.error('绑定手机号失败:', error)
  } finally {
    binding.value = false
  }
}

// 关闭手机号弹窗
const closePhoneModal = () => {
  showPhoneModal.value = false
  phoneForm.phone = ''
  phoneForm.code = ''
  countdown.value = 0
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #0a0a0a;
  padding: 40rpx;
}

.edit-card {
  background: #1a1a1a;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 40rpx;
  
  .label {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 20rpx;
  }
}

.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 30rpx;
  
  .avatar-preview {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
  }
  
  .upload-btn {
    flex: 1;
    padding: 20rpx 40rpx;
    background: rgba(0, 217, 163, 0.1);
    border: 2rpx solid rgba(0, 217, 163, 0.3);
    border-radius: 12rpx;
    text-align: center;
    color: #00d9a3;
    font-size: 28rpx;
  }
}

.input-dark {
  background: #2a2a2a;
  border: 2rpx solid #333;
  border-radius: 12rpx;
  color: #fff;
  padding: 24rpx;
  font-size: 28rpx;
  width: 100%;
}

.phone-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: #2a2a2a;
  border-radius: 12rpx;
  color: #fff;
  font-size: 28rpx;
  
  .verified-badge {
    padding: 8rpx 20rpx;
    background: rgba(0, 217, 163, 0.2);
    border-radius: 20rpx;
    color: #00d9a3;
    font-size: 24rpx;
  }
}

.phone-bind {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: rgba(0, 217, 163, 0.1);
  border: 2rpx solid rgba(0, 217, 163, 0.3);
  border-radius: 12rpx;
  
  .bind-text {
    color: #00d9a3;
    font-size: 28rpx;
  }
  
  .arrow {
    font-size: 40rpx;
    color: #00d9a3;
  }
}

.btn-save {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  margin-top: 40rpx;
}

.phone-modal {
  width: 600rpx;
  background: #1a1a1a;
  border-radius: 24rpx;
  padding: 40rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
    text-align: center;
    margin-bottom: 40rpx;
  }
  
  .modal-form {
    margin-bottom: 40rpx;
    
    .code-input-wrapper {
      display: flex;
      gap: 20rpx;
      margin-top: 20rpx;
      
      .code-input {
        flex: 1;
      }
      
      .btn-code {
        width: 200rpx;
        height: 80rpx;
        line-height: 80rpx;
        font-size: 24rpx;
        padding: 0;
      }
    }
  }
  
  .modal-actions {
    display: flex;
    gap: 20rpx;
    
    button {
      flex: 1;
      height: 80rpx;
      line-height: 80rpx;
      font-size: 28rpx;
    }
  }
}
</style>

