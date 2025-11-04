# SkyCanvas 前端项目

基于 uni-app 的跨端AI视频生成应用

## 技术栈

- **框架**: uni-app (Vue 3)
- **UI库**: uView UI 3.0
- **状态管理**: Pinia
- **构建工具**: Vite

## 支持平台

- ✅ 微信小程序
- ✅ H5
- ⚠️ App（未测试）
- ⚠️ 其他小程序（未测试）

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发

```bash
# 微信小程序
npm run dev:mp-weixin

# H5
npm run dev:h5
```

### 构建

```bash
# 微信小程序
npm run build:mp-weixin

# H5
npm run build:h5
```

## 项目结构

```
frontend/
├── src/
│   ├── api/                  # API接口
│   │   ├── auth.js           # 认证接口
│   │   ├── video.js          # 视频接口
│   │   └── credit.js         # 积分接口
│   ├── pages/                # 页面
│   │   ├── index/            # 首页
│   │   ├── generate/         # 生成页
│   │   ├── works/            # 作品页
│   │   ├── community/        # 社区页
│   │   ├── profile/          # 个人中心
│   │   └── credit/           # 积分相关
│   ├── stores/               # 状态管理
│   │   └── user.js           # 用户状态
│   ├── utils/                # 工具函数
│   │   └── request.js        # 网络请求
│   ├── static/               # 静态资源
│   ├── App.vue               # 应用入口
│   ├── main.js               # 主入口
│   ├── pages.json            # 页面配置
│   └── manifest.json         # 应用配置
├── package.json
├── vite.config.js
└── README.md
```

## 配置说明

### 1. 修改API地址

`src/utils/request.js`:

```javascript
const BASE_URL = process.env.NODE_ENV === 'development' 
  ? 'http://localhost:8080/api' 
  : 'https://api.skycanvas.com/api'
```

### 2. 微信小程序配置

`src/manifest.json`:

```json
{
  "mp-weixin": {
    "appid": "你的小程序appid"
  }
}
```

### 3. 主题色配置

`src/App.vue`:

```scss
:root {
  --primary-color: #00d9a3;
  --primary-gradient: linear-gradient(135deg, #4ade80 0%, #00d9a3 100%);
}
```

## 页面说明

### 首页 (pages/index)
- Banner展示
- 功能介绍
- 热门作品

### 生成页 (pages/generate)
- 输入参数Tab
  - 图片上传
  - 提示词输入
  - 参数选择（分辨率、时长）
- 历史记录Tab
  - 任务列表
  - 状态显示

### 作品页 (pages/works)
- 我的作品列表
- 作品详情
- 作品管理

### 社区页 (pages/community)
- 作品广场（开发中）

### 个人中心 (pages/profile)
- 用户信息
- 积分余额
- 功能入口

## 状态管理

使用 Pinia 管理全局状态：

```javascript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 登录
await userStore.login()

// 获取用户信息
const userInfo = userStore.userInfo

// 积分余额
const credits = userStore.credits
```

## 网络请求

统一使用封装的request方法：

```javascript
import { post, get } from '@/utils/request'

// GET请求
const data = await get('/api/endpoint', { param: 'value' })

// POST请求
const result = await post('/api/endpoint', { data: 'value' })
```

## 样式规范

### 1. 使用CSS变量

```scss
.button {
  background: var(--primary-gradient);
  color: var(--text-primary);
}
```

### 2. 单位使用rpx

```scss
.container {
  padding: 40rpx;
  font-size: 28rpx;
}
```

### 3. 暗黑主题

所有页面默认使用暗黑主题，背景色 `#0a0a0a`

## 常见问题

### 1. 小程序真机预览白屏

- 检查 `manifest.json` 中的 `appid`
- 检查后端API是否配置HTTPS
- 检查微信小程序合法域名配置

### 2. H5跨域问题

开发环境已配置代理，生产环境需后端配置CORS

### 3. 图片上传失败

检查OSS配置和上传接口

## 发布流程

### 微信小程序

1. `npm run build:mp-weixin`
2. 使用微信开发者工具打开 `dist/build/mp-weixin`
3. 点击"上传"
4. 登录微信公众平台提交审核

### H5

1. `npm run build:h5`
2. 将 `dist/build/h5` 目录部署到服务器
3. 配置Nginx

## License

MIT

