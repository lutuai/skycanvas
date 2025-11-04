# SkyCanvas 快速启动指南

## 🚀 5分钟快速体验

### 前置准备

确保已安装以下软件：

```bash
# 检查版本
java -version    # 需要 JDK 17+
mysql --version  # 需要 MySQL 8.0+
redis-server --version  # 需要 Redis 6.0+
node -v          # 需要 Node.js 14+
```

### 第一步：初始化数据库

```bash
# 1. 启动MySQL
sudo service mysql start

# 2. 创建数据库并导入数据
mysql -u root -p < database/schema.sql

# 验证：查看表是否创建成功
mysql -u root -p skycanvas -e "SHOW TABLES;"
```

### 第二步：启动Redis

```bash
redis-server
```

### 第三步：配置后端

```bash
cd backend

# 复制配置文件
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml

# 修改配置（最小配置）
# 编辑 application-dev.yml，修改以下内容：
# - 数据库密码
# - Redis密码（如果有）
```

### 第四步：启动后端

```bash
# 在backend目录下
mvn spring-boot:run

# 看到以下输出表示成功：
# ====================================
# SkyCanvas Backend Started Successfully!
# API地址: http://localhost:8080/api
# ====================================
```

### 第五步：启动前端

#### 方式一：H5开发（推荐快速体验）

```bash
cd frontend

# 安装依赖
npm install

# 启动H5
npm run dev:h5

# 浏览器访问: http://localhost:3000
```

#### 方式二：微信小程序开发

```bash
cd frontend

# 安装依赖
npm install

# 启动小程序编译
npm run dev:mp-weixin

# 使用微信开发者工具导入项目
# 目录: frontend/dist/dev/mp-weixin
```

### 第六步：测试功能

1. **登录测试**（H5版）
   - 由于H5无法使用微信登录，可以先跳过登录功能
   - 或修改代码添加测试账号登录

2. **查看界面**
   - 首页
   - 生成页（需要登录）
   - 个人中心

## 📝 开发模式配置

### 测试账号登录（可选）

为了方便H5开发测试，可以添加测试登录：

编辑 `backend/src/main/java/com/skycanvas/controller/AuthController.java`，添加测试接口：

```java
@PostMapping("/test-login")
public Result<UserInfoDTO> testLogin() {
    // 仅开发环境使用
    User user = userService.getUserByOpenid("test_openid");
    if (user == null) {
        user = new User();
        user.setOpenid("test_openid");
        user.setNickname("测试用户");
        user.setAvatar("https://via.placeholder.com/100");
        user.setCredits(1000);
        // 保存用户...
    }
    
    String token = jwtUtils.generateToken(user.getId());
    UserInfoDTO dto = new UserInfoDTO();
    BeanUtils.copyProperties(user, dto);
    dto.setToken(token);
    return Result.success(dto);
}
```

## 🔧 常见问题

### 1. 后端启动失败

**问题**: `Connection refused: connect`

**解决**: 检查MySQL和Redis是否启动

```bash
# 检查MySQL
sudo service mysql status

# 检查Redis
redis-cli ping
```

### 2. 前端无法访问后端

**问题**: 跨域错误或连接失败

**解决**: 
- 确保后端已启动（http://localhost:8080）
- 检查 `frontend/src/utils/request.js` 中的BASE_URL配置
- H5开发已配置代理，应该不会有跨域问题

### 3. 依赖安装失败

**问题**: `npm install` 报错

**解决**:
```bash
# 清除缓存
npm cache clean --force

# 使用国内镜像
npm install --registry=https://registry.npmmirror.com

# 或使用cnpm
npm install -g cnpm --registry=https://registry.npmmirror.com
cnpm install
```

### 4. Maven依赖下载慢

**解决**: 配置阿里云镜像

编辑 `~/.m2/settings.xml`:

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

## 📱 功能清单

- ✅ 用户登录（微信小程序）
- ✅ 用户信息展示
- ✅ 积分系统
- ✅ 视频生成任务提交
- ✅ 任务状态查询
- ✅ 生成历史记录
- ✅ 积分明细
- ⚠️ 微信支付（需要配置）
- ⚠️ 视频实际生成（需要Sora API）

## 🎯 下一步

1. **配置Sora API**
   - 获取Sora API密钥
   - 修改 `application.yml` 中的配置

2. **配置微信小程序**
   - 注册小程序
   - 获取AppID和AppSecret
   - 修改配置文件

3. **配置阿里云OSS**
   - 创建Bucket
   - 获取AccessKey
   - 修改配置文件

4. **部署到生产环境**
   - 参考 [DEPLOY.md](docs/DEPLOY.md)

## 💡 提示

- 开发过程中修改代码后，后端需要重启（或使用Spring Boot DevTools热重载）
- 前端修改会自动热更新
- 数据库表结构修改后需要重新导入SQL

## 📚 文档

- [API文档](docs/API.md)
- [数据库设计](docs/DATABASE.md)
- [部署文档](docs/DEPLOY.md)
- [后端README](backend/README.md)
- [前端README](frontend/README.md)

## 🆘 获取帮助

- 查看项目README
- 查看各模块的README
- 检查日志文件
- GitHub Issues（如有）

---

**祝开发愉快！** 🎉

