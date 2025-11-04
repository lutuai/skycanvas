# SkyCanvas - AI视频生成平台

基于Sora API的智能视频生成小程序，支持微信小程序、H5和Web多端访问。

## 技术栈

### 前端
- **uni-app (Vue 3)** - 跨端框架
- **uView UI 3.0** - UI组件库
- **Pinia** - 状态管理
- **Vite** - 构建工具

### 后端
- **Spring Boot 2.7+** - 后端框架
- **JDK 17+** - Java运行环境
- **MyBatis-Plus** - ORM框架
- **MySQL 8.0** - 数据库
- **Redis 6.0+** - 缓存
- **JWT** - 认证授权
- **阿里云OSS** - 对象存储

## 项目结构

```
SkyCanvas/
├── frontend/                 # uni-app前端项目
├── backend/                  # Spring Boot后端项目
├── database/                 # 数据库脚本
├── docs/                     # 项目文档
└── README.md
```

## 快速开始

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev:mp-weixin  # 微信小程序
npm run dev:h5         # H5
```

## 核心功能

- ✅ 微信授权登录
- ✅ 积分充值与消费
- ✅ AI视频生成（支持多provider）
- ✅ 作品管理
- ✅ 社区分享
- ✅ 微信支付

## 开发文档

- [API文档](./docs/API.md)
- [数据库设计](./docs/DATABASE.md)
- [部署文档](./docs/DEPLOY.md)

## License

MIT

