# SkyCanvas 开发指南

本文档包含前后端开发的详细说明，帮助开发者快速上手 SkyCanvas 项目。

---

## 📋 目录

- [后端开发](#后端开发)
  - [技术栈](#后端技术栈)
  - [项目结构](#后端项目结构)
  - [核心模块](#核心模块说明)
  - [开发规范](#开发规范)
- [前端开发](#前端开发)
  - [技术栈](#前端技术栈)
  - [项目结构](#前端项目结构)
  - [配置说明](#前端配置说明)
  - [页面说明](#页面说明)
  - [状态管理](#状态管理)
  - [样式规范](#样式规范)
- [常见问题](#常见问题)

---

## 后端开发

### 后端技术栈

- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0+
- **认证**: JWT
- **文件存储**: 阿里云OSS
- **构建工具**: Maven 3.6+
- **Java版本**: JDK 17+

### 后端核心功能

- ✅ 微信小程序登录
- ✅ 用户管理
- ✅ 积分系统
- ✅ AI视频生成（支持多provider）
- ✅ 任务队列管理
- ✅ 作品管理
- ⚠️ 微信支付（开发中）
- ⚠️ 社区功能（开发中）

### 后端快速开始

#### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

#### 2. 数据库初始化

```bash
mysql -u root -p < ../database/schema.sql
```

#### 3. 配置文件

复制配置文件模板：

```bash
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

修改配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/skycanvas
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379

wechat:
  miniapp:
    app-id: your_appid
    app-secret: your_secret
```

#### 4. 启动项目

```bash
mvn spring-boot:run
```

访问: http://localhost:8080/api

### 后端项目结构

```
backend/
├── src/main/java/com/skycanvas/
│   ├── SkyCanvasApplication.java     # 启动类
│   ├── common/                       # 公共类
│   │   ├── Result.java               # 统一响应
│   │   └── PageResult.java           # 分页响应
│   ├── config/                       # 配置类
│   │   ├── MyBatisPlusConfig.java    # MyBatis配置
│   │   ├── RedisConfig.java          # Redis配置
│   │   ├── CorsConfig.java           # 跨域配置
│   │   └── WebSocketConfig.java      # WebSocket配置
│   ├── controller/                   # 控制器
│   │   ├── AuthController.java       # 认证接口
│   │   ├── VideoController.java      # 视频接口
│   │   └── CreditController.java     # 积分接口
│   ├── dto/                          # 数据传输对象
│   ├── entity/                       # 实体类
│   │   ├── User.java                 # 用户
│   │   ├── VideoTask.java            # 视频任务
│   │   └── ...                       # 其他实体
│   ├── mapper/                       # MyBatis Mapper
│   ├── service/                      # 服务层
│   │   ├── UserService.java          # 用户服务
│   │   ├── VideoTaskService.java     # 视频任务服务
│   │   └── CreditService.java        # 积分服务
│   ├── video/                        # 视频生成服务
│   │   ├── VideoGenerationService.java  # 统一接口
│   │   ├── VideoProviderFactory.java    # Provider工厂
│   │   └── provider/                    # Provider实现
│   │       ├── SoraProxyProvider.java
│   │       └── SoraOfficialProvider.java
│   ├── interceptor/                  # 拦截器
│   │   └── AuthInterceptor.java      # 认证拦截器
│   ├── exception/                    # 异常处理
│   │   ├── BusinessException.java    # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   └── utils/                        # 工具类
│       ├── JwtUtils.java             # JWT工具
│       └── RedisUtils.java           # Redis工具
└── src/main/resources/
    ├── application.yml               # 主配置
    ├── application-dev.yml.example   # 开发配置模板
    └── mapper/                       # MyBatis XML
```

### 核心模块说明

#### 1. 视频生成服务（可无缝切换）

**设计模式**：适配器模式 + 策略模式 + 工厂模式

**接口定义**：

```java
public interface VideoGenerationService {
    VideoTaskDTO submitTask(VideoGenerationRequest request);
    VideoTaskDTO queryTask(String taskId);
    VideoTaskDTO getResult(String taskId);
    boolean cancelTask(String taskId);
    String getProviderName();
    boolean healthCheck();
}
```

**Provider实现**：

- `SoraProxyProvider`: 第三方Sora API中转站
- `SoraOfficialProvider`: OpenAI官方Sora API（待实现）

**切换Provider**：

修改配置文件：

```yaml
video:
  provider:
    default: sora-proxy  # 改为 sora-official 即可切换
```

#### 2. 积分系统

- **充值**: `CreditService.recharge(userId, amount, orderId)`
- **消费**: `CreditService.consume(userId, amount, taskId, description)`
- **退款**: `CreditService.refund(userId, amount, taskId)`

#### 3. 异步任务处理

```java
@Async
public void checkTaskStatusAsync(Long taskId, String providerTaskId, String providerName) {
    // 定期检查任务状态
    // 完成后更新数据库
    // 失败时退回积分
}
```

### 开发规范

#### 1. 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 注释清晰，方法有JavaDoc

#### 2. 接口规范

- RESTful设计
- 统一响应格式
- 错误码规范

#### 3. 数据库规范

- 表名：tb_前缀
- 字段名：下划线命名
- 必须有create_time、update_time
- 软删除使用deleted字段

#### 4. Git规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

### 后端常见问题

#### 1. 启动失败

- 检查MySQL是否启动
- 检查Redis是否启动
- 检查配置文件是否正确

#### 2. 数据库连接失败

- 检查数据库URL、用户名、密码
- 检查数据库是否创建
- 检查MySQL是否允许远程连接

#### 3. Redis连接失败

- 检查Redis是否启动
- 检查Redis密码配置
- 检查防火墙设置

### 性能优化建议

1. **数据库优化**
   - 添加索引
   - 分页查询
   - 避免N+1查询

2. **缓存策略**
   - 用户信息缓存30分钟
   - 热门数据缓存1小时
   - 使用Redis缓存

3. **异步处理**
   - 视频生成任务异步处理
   - 发送通知异步处理

4. **连接池配置**
   - Druid连接池
   - Redis连接池

---

## 前端开发

### 前端技术栈

- **框架**: uni-app (Vue 3)
- **UI库**: uView UI 3.0
- **状态管理**: Pinia
- **构建工具**: Vite

### 支持平台

- ✅ 微信小程序
- ✅ H5
- ⚠️ App（未测试）
- ⚠️ 其他小程序（未测试）

### 前端快速开始

#### 安装依赖

```bash
npm install
```

#### 开发

```bash
# 微信小程序
npm run dev:mp-weixin

# H5
npm run dev:h5
```

#### 构建

```bash
# 微信小程序
npm run build:mp-weixin

# H5
npm run build:h5
```

### 前端项目结构

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
│   │   │   ├── index.vue     # 个人中心首页
│   │   │   └── edit.vue      # 编辑个人信息
│   │   └── credit/           # 积分相关
│   │       ├── logs.vue      # 积分明细
│   │       └── recharge.vue  # 积分充值
│   ├── stores/               # 状态管理
│   │   └── user.js           # 用户状态
│   ├── utils/                # 工具函数
│   │   └── request.js        # 网络请求
│   ├── static/               # 静态资源
│   │   └── tabbar/           # 底部导航图标
│   ├── App.vue               # 应用入口
│   ├── main.js               # 主入口
│   ├── pages.json            # 页面配置
│   └── manifest.json         # 应用配置
├── package.json
├── vite.config.js
└── README.md
```

### 前端配置说明

#### 1. 修改API地址

`src/utils/request.js`:

```javascript
const BASE_URL = process.env.NODE_ENV === 'development' 
  ? 'http://localhost:8080/api' 
  : 'https://api.skycanvas.com/api'
```

#### 2. 微信小程序配置

`src/manifest.json`:

```json
{
  "mp-weixin": {
    "appid": "你的小程序appid"
  }
}
```

#### 3. 主题色配置

`src/App.vue`:

```scss
:root {
  --primary-color: #00d9a3;
  --primary-gradient: linear-gradient(135deg, #4ade80 0%, #00d9a3 100%);
}
```

### 页面说明

#### 首页 (pages/index)
- Banner展示
- 功能介绍
- 热门作品

#### 生成页 (pages/generate)
- 输入参数Tab
  - 图片上传
  - 提示词输入
  - 参数选择（分辨率、时长）
- 历史记录Tab
  - 任务列表
  - 状态显示

#### 作品页 (pages/works)
- 我的作品列表
- 作品详情
- 作品管理

#### 社区页 (pages/community)
- 作品广场（开发中）

#### 个人中心 (pages/profile)
- 用户信息
- 积分余额
- 功能入口

#### 个人信息编辑 (pages/profile/edit)
- 头像上传
- 昵称编辑
- 手机号绑定

#### 积分明细 (pages/credit/logs)
- 积分变动记录
- 充值/消费/退款记录

#### 积分充值 (pages/credit/recharge)
- 积分套餐选择
- 微信支付（开发中）

### 状态管理

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

// 更新用户信息
await userStore.updateUserInfo({
  nickname: '新昵称',
  avatar: '新头像URL'
})

// 绑定手机号
await userStore.sendSmsCode('13800138000')
await userStore.bindPhone({
  phone: '13800138000',
  code: '123456'
})
```

### 网络请求

统一使用封装的request方法：

```javascript
import { post, get } from '@/utils/request'

// GET请求
const data = await get('/api/endpoint', { param: 'value' })

// POST请求
const result = await post('/api/endpoint', { data: 'value' })
```

### 样式规范

#### 1. 使用CSS变量

```scss
.button {
  background: var(--primary-gradient);
  color: var(--text-primary);
}
```

#### 2. 单位使用rpx

```scss
.container {
  padding: 40rpx;
  font-size: 28rpx;
}
```

#### 3. 暗黑主题

所有页面默认使用暗黑主题，背景色 `#0a0a0a`

### 前端常见问题

#### 1. 小程序真机预览白屏

- 检查 `manifest.json` 中的 `appid`
- 检查后端API是否配置HTTPS
- 检查微信小程序合法域名配置

#### 2. H5跨域问题

开发环境已配置代理，生产环境需后端配置CORS

#### 3. 图片上传失败

检查OSS配置和上传接口

### 发布流程

#### 微信小程序

1. `npm run build:mp-weixin`
2. 使用微信开发者工具打开 `dist/build/mp-weixin`
3. 点击"上传"
4. 登录微信公众平台提交审核

#### H5

1. `npm run build:h5`
2. 将 `dist/build/h5` 目录部署到服务器
3. 配置Nginx

---

## 常见问题

### JDK版本问题

项目使用 JDK 17，如需升级：

**Windows安装JDK 17**：
1. 下载：https://adoptium.net/zh-CN/temurin/releases/?version=17
2. 安装并配置JAVA_HOME环境变量
3. 验证：`java -version`

**配置环境变量**：
```
JAVA_HOME=C:\Program Files\Java\jdk-17
Path=%JAVA_HOME%\bin
```

### 脚本权限问题（Linux/Mac）

```bash
# 赋予执行权限
chmod +x scripts/*.sh
```

### 端口占用

- 后端默认端口：8080
- 前端默认端口：3000

检查端口占用：
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

---

## 📚 相关文档

- [API文档](API.md) - 接口详细说明
- [数据库文档](DATABASE.md) - 数据库设计
- [部署文档](DEPLOY.md) - 部署指南

---

## License

MIT

