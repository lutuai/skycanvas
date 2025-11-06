/**
 * 自定义深色主题弹窗工具
 * 替代 uni.showModal，提供符合应用深色风格的弹窗
 * 
 * 使用说明：
 * 1. 在 App.vue 中已经全局引入 CustomModal 组件
 * 2. 页面中导入并使用 showCustomModal 方法即可
 * 3. 弹窗样式自动适配深色主题，保持应用风格统一
 */

let modalInstance = null

/**
 * 显示自定义深色主题弹窗（推荐使用）
 * 这个方法会使用全局的 CustomModal 组件，样式完全符合深色主题
 * @param {Object} options - 配置选项
 * @param {String} options.title - 标题，默认"提示"
 * @param {String} options.content - 内容
 * @param {String} options.confirmText - 确认按钮文字，默认"确定"
 * @param {String} options.cancelText - 取消按钮文字，默认"取消"
 * @param {Boolean} options.showCancel - 是否显示取消按钮，默认true
 * @param {Boolean} options.closeOnClickOverlay - 点击遮罩层是否关闭，默认true
 * @returns {Promise<Boolean>} 返回Promise，resolve(true)表示点击确认，resolve(false)表示点击取消
 * 
 * @example
 * import { showCustomModal } from '@/utils/modal'
 * 
 * // 确认弹窗
 * const confirm = await showCustomModal({
 *   title: '提示',
 *   content: '确定要删除吗？',
 *   confirmText: '删除'
 * })
 * if (confirm) {
 *   // 用户点击了确定
 * }
 * 
 * // 提示弹窗（只有确定按钮）
 * await showCustomModal({
 *   title: '提示',
 *   content: '操作成功',
 *   showCancel: false
 * })
 */
export function showCustomModal(options = {}) {
  // 添加延迟检查，给 CustomModal 一些初始化时间
  return new Promise((resolve) => {
    const maxRetry = 10
    let retryCount = 0
    
    const checkAndShow = () => {
      if (modalInstance) {
        // 实例已准备好，显示弹窗
        modalInstance.show(options).then(resolve)
      } else if (retryCount < maxRetry) {
        // 等待 50ms 后重试
        retryCount++
        setTimeout(checkAndShow, 50)
      } else {
        // 超时，降级到系统弹窗
        console.warn('CustomModal 未初始化，降级使用系统弹窗')
        showSystemModal(options).then(resolve)
      }
    }
    
    checkAndShow()
  })
}

/**
 * 系统原生弹窗（兼容降级方案，不推荐使用）
 * 注意：小程序中此弹窗为白色背景，与深色主题不符
 * @private
 */
function showSystemModal(options = {}) {
  return new Promise((resolve) => {
    const {
      title = '提示',
      content = '',
      confirmText = '确定',
      cancelText = '取消',
      showCancel = true,
      confirmColor = '#00d9a3',
      cancelColor = '#999999'
    } = options

    uni.showModal({
      title,
      content,
      confirmText,
      cancelText,
      showCancel,
      confirmColor,
      cancelColor,
      success: (res) => {
        resolve(res.confirm)
      },
      fail: () => {
        resolve(false)
      }
    })
  })
}

/**
 * 设置 modal 实例（由 CustomModal 组件调用）
 * @private
 */
export function setModalInstance(instance) {
  modalInstance = instance
}

/**
 * 显示成功提示
 */
export function showSuccess(message, duration = 2000) {
  uni.showToast({
    title: message,
    icon: 'success',
    duration
  })
}

/**
 * 显示错误提示
 */
export function showError(message, duration = 2000) {
  uni.showToast({
    title: message,
    icon: 'none',
    duration
  })
}

/**
 * 显示加载提示
 */
export function showLoading(message = '加载中...') {
  uni.showLoading({
    title: message,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
  uni.hideLoading()
}

export default {
  showCustomModal,
  setModalInstance,
  showSuccess,
  showError,
  showLoading,
  hideLoading
}

