# 组件说明

## CustomModal - 深色主题弹窗组件

### 功能说明

这是一个自定义的深色主题弹窗组件，用于替代 `uni.showModal` 系统API。

### 设计特点

1. **深色主题适配**
   - 背景色：`#1a1a1a`（深灰色）
   - 遮罩层：`rgba(0, 0, 0, 0.7)`
   - 分割线：`#333`

2. **品牌色应用**
   - 确认按钮：`#00d9a3`（主题绿色）
   - 取消按钮：`#999`（中性灰）

3. **动画效果**
   - 遮罩层淡入动画（0.2s）
   - 内容上滑动画（0.3s）
   - 流畅的用户体验

4. **交互优化**
   - 支持点击遮罩层关闭
   - 按钮点击态反馈
   - 自适应内容高度

### 使用方法

**不需要在页面中引入**，已在 `App.vue` 中全局注册。

```javascript
import { showCustomModal } from '@/utils/modal'

// 确认对话框
const result = await showCustomModal({
  title: '确认删除',
  content: '删除后无法恢复，确定要删除吗？',
  confirmText: '删除',
  cancelText: '取消'
})

if (result) {
  // 用户点击了确定
  console.log('确认删除')
}

// 提示对话框（只有确定按钮）
await showCustomModal({
  title: '提示',
  content: '操作成功',
  showCancel: false,
  confirmText: '知道了'
})
```

### 配置选项

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | String | '提示' | 弹窗标题 |
| content | String | '' | 弹窗内容 |
| confirmText | String | '确定' | 确认按钮文字 |
| cancelText | String | '取消' | 取消按钮文字 |
| showCancel | Boolean | true | 是否显示取消按钮 |
| closeOnClickOverlay | Boolean | true | 点击遮罩是否关闭 |

### 返回值

- `Promise<Boolean>`
  - `true` - 用户点击了确定
  - `false` - 用户点击了取消或遮罩层

### 样式规范

**颜色变量：**
```scss
--bg-modal: #1a1a1a;        // 弹窗背景
--text-title: #fff;          // 标题颜色
--text-content: #ccc;        // 内容颜色
--text-cancel: #999;         // 取消按钮颜色
--text-confirm: #00d9a3;     // 确认按钮颜色
--border-color: #333;        // 分割线颜色
```

**尺寸：**
- 弹窗宽度：`560rpx`
- 圆角：`24rpx`
- 标题字号：`32rpx`
- 内容字号：`28rpx`
- 按钮高度：`100rpx`

### 注意事项

1. 此组件已在 App.vue 中全局注册，无需重复引入
2. 使用 async/await 语法处理异步结果
3. 建议统一使用此组件，不要混用 `uni.showModal`

