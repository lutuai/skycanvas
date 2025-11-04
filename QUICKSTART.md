# 🚀 SkyCanvas 快速启动指南

> 5分钟快速启动 AI视频生成平台

## 📋 环境检查

### 必需软件

- ✅ **JDK 17+** - 后端运行环境
- ✅ **Maven 3.6+** - 后端构建工具
- ✅ **MySQL 8.0+** - 数据库
- ✅ **Redis 6.0+** - 缓存
- ✅ **Node.js 14+** - 前端运行环境

### 检查命令

```bash
java -version      # 应显示 17.x.x
mvn -version       # 应显示 3.6+
mysql --version    # 应显示 8.0+
redis-server --version
node -v && npm -v
```

---

## ⚡ 三步启动（Windows）

### 第一步：初始化数据库

**使用脚本（推荐）**
```bash
# 双击运行
init-database.bat
```

**手动执行**
```bash
# 导入数据库
mysql -u root -p < database\schema.sql
```

### 第二步：启动后端

**使用脚本（推荐）**
```bash
# 双击运行
start-backend.bat
```

**手动执行**
```bash
cd backend

# 1. 复制配置文件
copy src\main\resources\application-dev.yml.example src\main\resources\application-dev.yml

# 2. 编辑配置文件（修改数据库密码等）
notepad src\main\resources\application-dev.yml

# 3. 启动Redis（新窗口）
redis-server

# 4. 启动后端
mvn spring-boot:run
```

✅ 看到以下输出表示成功：
```
====================================
SkyCanvas Backend Started Successfully!
API地址: http://localhost:8080/api
====================================
```

### 第三步：启动前端

**使用脚本（推荐）**
```bash
# 双击运行
start-frontend.bat

# 选择：1-H5浏览器版 / 2-微信小程序版
```

**手动执行**
```bash
cd frontend

# 安装依赖
npm install

# H5版（浏览器访问）
npm run dev:h5
# 访问: http://localhost:3000

# 小程序版
npm run dev:mp-weixin
# 用微信开发者工具导入: frontend\dist\dev\mp-weixin
```

---

## 🌐 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端API | http://localhost:8080/api | Spring Boot服务 |
| H5前端 | http://localhost:3000 | 浏览器访问 |
| 小程序 | 微信开发者工具 | 导入dist\dev\mp-weixin |

---

## 🔧 最小配置

### 后端配置（application-dev.yml）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/skycanvas
    username: root
    password: 你的MySQL密码  # ⚠️ 必填
  
  redis:
    host: localhost
    port: 6379
    password: ""  # 如果Redis有密码，填写这里
```

### 前端配置（可选）

如需修改API地址，编辑 `frontend\src\utils\request.js`：

```javascript
const BASE_URL = 'http://localhost:8080/api'
```

---

## ⚠️ 常见问题

### 后端启动失败

**问题**: `Communications link failure`
```
✅ 解决: 
1. 检查MySQL是否启动
2. 检查数据库密码是否正确
3. 验证数据库是否创建成功：
   mysql -u root -p
   USE skycanvas;
   SHOW TABLES;
```

**问题**: `Connection refused: connect` (Redis)
```
✅ 解决:
1. 启动Redis: redis-server
2. 或暂时注释配置文件中的Redis相关配置（仅测试用）
```

### 前端启动失败

**问题**: `npm install` 失败
```
✅ 解决:
# 方式1: 清除缓存
npm cache clean --force
npm install

# 方式2: 使用国内镜像
npm install --registry=https://registry.npmmirror.com

# 方式3: 使用cnpm
npm install -g cnpm --registry=https://registry.npmmirror.com
cnpm install
```

**问题**: 页面空白或报错
```
✅ 检查:
1. 后端是否启动（访问 http://localhost:8080/api）
2. 浏览器控制台报错信息（F12 - Console）
3. 网络请求是否正常（F12 - Network）
```

### 数据库相关

**问题**: 表不存在
```
✅ 解决: 重新导入数据库
mysql -u root -p
DROP DATABASE IF EXISTS skycanvas;
SOURCE database/schema.sql;
```

---

## 📱 功能测试

### 1. 测试后端API

浏览器访问：
```
http://localhost:8080/api/credit/balance
```
预期结果：返回 401 未授权（说明后端正常）

### 2. 测试前端页面

H5访问：`http://localhost:3000`

预期结果：看到暗黑主题的首页

### 3. 完整功能测试（需配置）

- ⚠️ 微信登录（需配置微信AppID/Secret）
- ✅ 查看积分明细
- ⚠️ 提交视频生成任务（需配置Sora API密钥）

---

## 📚 下一步

### 查看文档
- [API文档](docs/API.md) - 接口说明
- [数据库文档](docs/DATABASE.md) - 表结构
- [部署文档](docs/DEPLOY.md) - 生产部署
- [项目状态](PROJECT_STATUS.md) - 完成度报告

### 配置API密钥（可选）
- 微信小程序 AppID/Secret
- Sora API密钥
- 阿里云OSS配置

### 开始开发
- 后端代码：`backend\src\main\java\com\skycanvas`
- 前端代码：`frontend\src\pages`

---

## 🆘 需要帮助？

**检查清单**：
1. ✅ 环境是否正确安装
2. ✅ 服务是否正常启动
3. ✅ 配置文件是否正确
4. ✅ 端口是否被占用

**文档索引**：
- 快速启动问题 → 本文档
- API调用问题 → [API.md](docs/API.md)
- 数据库问题 → [DATABASE.md](docs/DATABASE.md)
- 部署问题 → [DEPLOY.md](docs/DEPLOY.md)

---

**🎉 祝你使用愉快！**
