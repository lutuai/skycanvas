# SkyCanvas - AI视频生成平台

> 基于Sora API的智能视频生成小程序，支持微信小程序、H5和Web多端访问

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7+-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JDK-17+-blue.svg" alt="JDK">
  <img src="https://img.shields.io/badge/uni--app-Vue%203-brightgreen.svg" alt="uni-app">
  <img src="https://img.shields.io/badge/完成度-85%25-yellow.svg" alt="完成度">
</p>

---

## 📖 快速导航

- [快速开始](#-快速开始) - 5分钟启动项目
- [技术栈](#-技术栈) - 技术选型
- [核心功能](#-核心功能) - 已实现功能
- [项目结构](#-项目结构) - 目录说明
- [文档索引](#-文档索引) - 详细文档

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 14+

### 三步启动（Windows）

```bash
# 1. 初始化数据库
mysql -u root -p < database\schema.sql

# 2. 启动后端
cd backend
mvn spring-boot:run

# 3. 启动前端
cd frontend
npm install
npm run dev:h5  # 或 npm run dev:mp-weixin
```

**或使用一键脚本**：
- 双击 `init-database.bat` - 初始化数据库
- 双击 `start-backend.bat` - 启动后端
- 双击 `start-frontend.bat` - 启动前端

📚 **详细教程**: [快速启动指南](QUICKSTART.md)

---

## 🛠️ 技术栈

### 前端
- **uni-app (Vue 3)** - 跨端框架
- **uView UI 3.0** - UI组件库
- **Pinia** - 状态管理
- **Vite** - 构建工具

### 后端
- **Spring Boot 2.7** - 后端框架
- **JDK 17** - Java运行环境
- **MyBatis-Plus** - ORM框架
- **MySQL 8.0** - 数据库
- **Redis 6.0+** - 缓存
- **JWT** - 认证授权

### 云服务
- **阿里云OSS** - 对象存储
- **阿里云ECS** - 服务器
- **微信支付** - 支付系统

---

## ✨ 核心功能

| 功能 | 状态 | 说明 |
|------|------|------|
| 微信授权登录 | ✅ 完成 | JWT认证 + Token拦截 |
| 积分充值与消费 | ✅ 完成 | 充值/消费/退款机制 |
| AI视频生成 | ⚠️ 70% | Provider架构，支持多AI服务 |
| 任务队列管理 | ✅ 完成 | 异步处理 + 状态同步 |
| 作品管理 | ⚠️ 40% | 实体完成，Service待开发 |
| 社区分享 | ⚠️ 30% | 数据库设计完成 |
| 微信支付 | ⚠️ 60% | 框架完成，待集成 |

**完成度**: 85% - MVP已完成，可开始集成测试

📊 **详细状态**: [项目状态报告](PROJECT_STATUS.md)

---

## 📁 项目结构

```
SkyCanvas/
├── backend/                 # Spring Boot后端
│   ├── src/main/java/
│   │   └── com/skycanvas/
│   │       ├── controller/  # 控制器
│   │       ├── service/     # 服务层
│   │       ├── entity/      # 实体类
│   │       └── video/       # 视频生成服务（核心）
│   └── pom.xml
│
├── frontend/                # uni-app前端
│   ├── src/
│   │   ├── pages/          # 页面（7个）
│   │   ├── api/            # API接口
│   │   ├── stores/         # Pinia状态
│   │   └── utils/          # 工具函数
│   └── package.json
│
├── database/               # 数据库脚本
│   └── schema.sql          # 建表SQL（9张表）
│
├── docs/                   # 项目文档
│   ├── API.md              # API文档
│   ├── DATABASE.md         # 数据库设计
│   └── DEPLOY.md           # 部署指南
│
├── QUICKSTART.md           # 快速启动指南
├── PROJECT_STATUS.md       # 项目状态报告
└── README.md               # 本文件
```

---

## 📚 文档索引

### 新手入门
- [快速启动指南](QUICKSTART.md) - 5分钟启动项目
- [后端README](backend/README.md) - 后端开发指南
- [前端README](frontend/README.md) - 前端开发指南

### 开发文档
- [API文档](docs/API.md) - 接口说明（15+接口）
- [数据库设计](docs/DATABASE.md) - 表结构（9张表）
- [部署文档](docs/DEPLOY.md) - 本地/生产部署

### 项目管理
- [项目状态报告](PROJECT_STATUS.md) - 完成度 + 下一步计划
- [JDK升级说明](docs/JDK_UPGRADE.md) - JDK 11→17升级指南

---

## 🌟 核心亮点

### 1. 可无缝切换的Provider架构

支持多种AI视频服务，一键切换：

```yaml
video:
  provider:
    default: sora-proxy  # 或 sora-official
```

### 2. 完整的积分系统

- ✅ 自动扣费
- ✅ 失败退款
- ✅ 消费记录
- ✅ 套餐充值

### 3. 跨端统一

一套代码，多端运行：
- 微信小程序
- H5（移动端）
- H5（PC端）

---

## 🎯 开发路线图

### ✅ 已完成（85%）
- [x] 基础架构搭建
- [x] 用户认证系统
- [x] 积分系统
- [x] 视频任务管理
- [x] 7个核心页面
- [x] 完整文档

### 🚧 进行中
- [ ] Sora API集成（需API密钥）
- [ ] 微信支付集成（需商户号）
- [ ] 阿里云OSS集成

### 📅 计划中
- [ ] 作品管理完善
- [ ] 社区功能开发
- [ ] 性能优化
- [ ] 单元测试

---

## 🔧 配置说明

### 必需配置

1. **数据库**：修改 `backend/src/main/resources/application-dev.yml`
   ```yaml
   spring:
     datasource:
       password: 你的MySQL密码
   ```

2. **Redis**：如有密码，修改配置文件

### 可选配置（功能依赖）

- 微信小程序：AppID + AppSecret
- Sora API：API密钥
- 阿里云OSS：AccessKey
- 微信支付：商户号 + 密钥

---

## 📞 获取帮助

### 遇到问题？

1. 查看 [快速启动指南](QUICKSTART.md) 的常见问题章节
2. 查看对应模块的README
3. 检查日志文件

### 文档快速查找

| 问题类型 | 查看文档 |
|---------|---------|
| 启动失败 | [QUICKSTART.md](QUICKSTART.md) |
| API调用 | [API.md](docs/API.md) |
| 数据库 | [DATABASE.md](docs/DATABASE.md) |
| 部署 | [DEPLOY.md](docs/DEPLOY.md) |
| 完成度 | [PROJECT_STATUS.md](PROJECT_STATUS.md) |

---

## 📄 License

MIT License

---

## 🎉 总结

SkyCanvas是一个**架构清晰、文档完善、可快速迭代**的AI视频生成平台。

**项目亮点**：
- 🏗️ 可扩展的Provider架构
- 📱 跨端统一的前端方案
- 📚 完整的开发文档
- 🚀 开箱即用的启动脚本

**立即开始**: 查看 [快速启动指南](QUICKSTART.md) 👈
