# SkyCanvas 后端项目

基于 Spring Boot 的AI视频生成平台后端服务

## 技术栈

- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0+
- **认证**: JWT
- **文件存储**: 阿里云OSS
- **构建工具**: Maven 3.6+

## 核心功能

- ✅ 微信小程序登录
- ✅ 用户管理
- ✅ 积分系统
- ✅ AI视频生成（支持多provider）
- ✅ 任务队列管理
- ✅ 作品管理
- ⚠️ 微信支付（开发中）
- ⚠️ 社区功能（开发中）

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库初始化

```bash
mysql -u root -p < ../database/schema.sql
```

### 3. 配置文件

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

### 4. 启动项目

```bash
mvn spring-boot:run
```

访问: http://localhost:8080/api

## 项目结构

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
│   │   └── GlobalExceptionHandler.java
│   └── utils/                        # 工具类
│       ├── JwtUtils.java             # JWT工具
│       └── RedisUtils.java           # Redis工具
└── src/main/resources/
    ├── application.yml               # 主配置
    ├── application-dev.yml.example   # 开发配置模板
    └── mapper/                       # MyBatis XML
```

## 核心模块说明

### 1. 视频生成服务（可无缝切换）

设计模式：适配器模式 + 策略模式 + 工厂模式

#### 接口定义

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

#### Provider实现

- `SoraProxyProvider`: 第三方Sora API中转站
- `SoraOfficialProvider`: OpenAI官方Sora API（待实现）

#### 切换Provider

修改配置文件：

```yaml
video:
  provider:
    default: sora-proxy  # 改为 sora-official 即可切换
```

### 2. 积分系统

- **充值**: `CreditService.recharge(userId, amount, orderId)`
- **消费**: `CreditService.consume(userId, amount, taskId, description)`
- **退款**: `CreditService.refund(userId, amount, taskId)`

### 3. 异步任务处理

```java
@Async
public void checkTaskStatusAsync(Long taskId, String providerTaskId, String providerName) {
    // 定期检查任务状态
    // 完成后更新数据库
    // 失败时退回积分
}
```

## API文档

详见 [API.md](../docs/API.md)

## 数据库设计

详见 [DATABASE.md](../docs/DATABASE.md)

## 部署说明

详见 [DEPLOY.md](../docs/DEPLOY.md)

## 开发规范

### 1. 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 注释清晰，方法有JavaDoc

### 2. 接口规范

- RESTful设计
- 统一响应格式
- 错误码规范

### 3. 数据库规范

- 表名：tb_前缀
- 字段名：下划线命名
- 必须有create_time、update_time
- 软删除使用deleted字段

### 4. Git规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

## 常见问题

### 1. 启动失败

- 检查MySQL是否启动
- 检查Redis是否启动
- 检查配置文件是否正确

### 2. 数据库连接失败

- 检查数据库URL、用户名、密码
- 检查数据库是否创建
- 检查MySQL是否允许远程连接

### 3. Redis连接失败

- 检查Redis是否启动
- 检查Redis密码配置
- 检查防火墙设置

## 性能优化建议

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

## License

MIT

