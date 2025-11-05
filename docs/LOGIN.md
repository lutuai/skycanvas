# 登录系统设计文档

## 📋 概述

SkyCanvas 支持多平台、多方式的登录系统，确保用户在不同环境下都能顺畅登录。

## 🎯 支持的登录方式

### 1. 微信小程序登录
- **流程**: wx.login获取code → 后端调用微信API获取openid → 创建/更新用户 → 返回JWT Token
- **特点**: 
  - 无需用户输入任何信息
  - 自动获取微信昵称和头像
  - 首次登录自动注册
  - 赠送新用户积分

### 2. H5设备ID登录
- **流程**: 生成唯一设备ID → 后端使用设备ID作为openid → 创建/更新用户 → 返回JWT Token
- **特点**: 
  - 适用于H5环境快速登录
  - 无需第三方授权
  - 设备ID本地持久化
  - 适合游客模式

### 3. 手机号+验证码登录
- **流程**: 输入手机号 → 获取验证码 → 验证码校验 → 创建/查询用户 → 返回JWT Token
- **特点**: 
  - 支持新用户注册
  - 验证码5分钟有效
  - 60秒防刷机制
  - 可作为绑定手机号的入口

## 🏗️ 系统架构

### 后端架构

```
┌─────────────────────────────────────────────────┐
│              AuthController                      │
│  - POST /auth/login (微信/H5登录)                │
│  - POST /auth/login/phone (手机号登录)           │
│  - POST /auth/sms/code (发送验证码)              │
│  - GET /auth/userinfo (获取用户信息)             │
│  - POST /auth/phone/bind (绑定手机号)            │
│  - POST /auth/token/refresh (刷新Token)          │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│              UserService                         │
│  - login() 微信/H5登录                           │
│  - loginByPhone() 手机号登录                     │
│  - sendSmsCode() 发送验证码                      │
│  - getUserInfo() 获取用户信息                    │
│  - bindPhone() 绑定手机号                        │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│         LoginLogService                          │
│  - recordLoginSuccess() 记录成功日志             │
│  - recordLoginFail() 记录失败日志                │
└─────────────────────────────────────────────────┘
```

### 前端架构

```
┌─────────────────────────────────────────────────┐
│           登录页面 (pages/login)                 │
│  - 手机号登录表单                                │
│  - 快速登录按钮                                  │
│  - 验证码发送与倒计时                            │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│              UserStore (Pinia)                   │
│  - login() 通用登录入口                          │
│  - loginByWeixin() 微信登录                      │
│  - loginByH5() H5登录                            │
│  - loginByPhoneCode() 手机号登录                 │
│  - saveLoginData() 保存登录状态                  │
│  - loadUserInfo() 加载用户信息                   │
│  - checkLogin() 检查登录状态                     │
│  - logout() 退出登录                             │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│              API层 (api/auth)                    │
│  - login() 登录接口                              │
│  - loginByPhone() 手机号登录接口                 │
│  - getUserInfo() 获取用户信息                    │
│  - sendSmsCode() 发送验证码                      │
└─────────────────────────────────────────────────┘
```

## 🔐 安全设计

### 1. Token机制
- **JWT Token**: 用户登录后生成JWT，包含userId等信息
- **有效期**: Token默认7天有效期
- **刷新机制**: 支持Token刷新接口
- **存储**: Token存储在localStorage和Pinia Store

### 2. 验证码安全
- **有效期**: 5分钟
- **频率限制**: 60秒内不能重复发送
- **一次性**: 验证成功后立即删除
- **Redis存储**: 验证码存储在Redis中

### 3. 拦截器
- **请求拦截**: 自动在header中添加Authorization
- **响应拦截**: 401自动清除登录状态，引导重新登录
- **错误处理**: 统一错误提示

## 📊 数据库设计

### 用户表 (tb_user)
```sql
- id: 用户ID
- openid: 微信openid/设备ID/手机号ID
- nickname: 用户昵称
- avatar: 用户头像
- phone: 手机号（可选）
- credits: 积分余额
- status: 用户状态（0-正常，1-禁用）
- create_time: 创建时间
```

### 登录日志表 (tb_login_log)
```sql
- id: 日志ID
- user_id: 用户ID
- login_ip: 登录IP
- device_type: 设备类型
- browser: 浏览器
- os: 操作系统
- status: 登录状态（0-失败，1-成功）
- fail_reason: 失败原因
- create_time: 创建时间
```

## 🔄 登录流程详解

### 微信小程序登录流程
```
1. 前端调用 wx.login 获取 code
2. （可选）调用 wx.getUserProfile 获取用户信息
3. 发送 code、nickname、avatar 到后端
4. 后端调用微信API用 code 换取 openid
5. 根据 openid 查询或创建用户
6. 生成 JWT Token
7. 返回用户信息 + Token
8. 前端保存到 Store 和 Storage
9. 记录登录日志
10. 触发 userLoginSuccess 事件
```

### 手机号登录流程
```
1. 用户输入手机号
2. 点击"获取验证码"
3. 后端发送短信验证码，存储到Redis（5分钟有效）
4. 用户输入验证码
5. 提交手机号 + 验证码
6. 后端验证验证码
7. 根据手机号查询或创建用户
8. 生成 JWT Token
9. 删除验证码缓存
10. 返回用户信息 + Token
11. 前端保存到 Store 和 Storage
12. 记录登录日志
13. 触发 userLoginSuccess 事件
```

### H5登录流程
```
1. 前端生成唯一设备ID（如不存在）
2. 发送设备ID到后端（作为code）
3. 后端使用 "h5_" + 设备ID 作为 openid
4. 根据 openid 查询或创建用户
5. 生成 JWT Token
6. 返回用户信息 + Token
7. 前端保存到 Store 和 Storage
8. 记录登录日志
9. 触发 userLoginSuccess 事件
```

## 🎨 前端响应式更新

### 问题
登录成功后，个人中心页面不会自动更新显示用户信息。

### 解决方案
1. **使用对象展开运算符**: `this.userInfo = { ...data }` 确保Vue响应式系统能检测到变化
2. **事件总线**: 登录成功后触发 `uni.$emit('userLoginSuccess')` 事件
3. **页面监听**: 个人中心等页面监听此事件，收到后刷新用户信息
4. **计算属性**: 使用 `computed` 获取 userInfo，自动响应状态变化

### 示例代码
```javascript
// Store中
saveLoginData(data) {
  this.userInfo = { ...data }  // 确保响应式
  uni.$emit('userLoginSuccess')  // 触发事件
}

// 页面中
onMounted(() => {
  uni.$on('userLoginSuccess', () => {
    userStore.loadUserInfo()  // 刷新用户信息
  })
})

const userInfo = computed(() => userStore.userInfo)  // 响应式获取
```

## 🚀 使用示例

### 1. 检查登录状态
```javascript
// 在需要登录的操作前调用
if (!await userStore.checkLogin()) {
  return  // 未登录，已引导用户去登录
}
// 已登录，继续业务逻辑
```

### 2. 手动调用登录
```javascript
// 跳转到登录页
uni.navigateTo({
  url: '/pages/login/index'
})

// 或直接调用登录（仅快速登录）
await userStore.login()
```

### 3. 获取用户信息
```javascript
// 使用computed自动响应
const userInfo = computed(() => userStore.userInfo)
const credits = computed(() => userStore.credits)

// 或直接访问
console.log(userStore.userInfo)
```

### 4. 退出登录
```javascript
userStore.logout()
// 登录状态已清除，需要重新登录
```

## 📝 待优化项

1. **短信服务集成**: 当前验证码仅打印日志，需集成真实短信服务
2. **IP地理位置**: 登录日志中的地理位置信息待完善
3. **设备指纹**: 增强H5登录的设备识别
4. **Token刷新**: 实现Token过期前自动刷新
5. **多设备管理**: 支持查看和管理登录设备
6. **登录通知**: 异地登录提醒

## 🔧 配置说明

### application-dev.yml
```yaml
# 微信小程序配置
wechat:
  miniapp:
    app-id: your_appid
    app-secret: your_secret

# 新用户注册奖励积分
user:
  register:
    bonus-credits: 100
```

### Redis配置
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 
```

## 📖 API文档

详见 [API.md](./API.md) 中的认证模块部分。

## 🐛 常见问题

### Q1: 登录成功但页面不更新？
A: 确保使用了对象展开运算符 `{ ...data }` 和事件监听机制。

### Q2: Token过期怎么办？
A: 响应拦截器会自动处理401，清除登录状态并引导重新登录。

### Q3: 如何测试H5登录？
A: 在浏览器中打开，会自动生成设备ID并使用H5登录方式。

### Q4: 验证码收不到？
A: 开发环境下验证码会打印在后端日志中，生产环境需配置短信服务。

### Q5: 如何区分新老用户？
A: 登录接口返回的日志中有 `isNewUser` 标识，可用于统计和引导。

