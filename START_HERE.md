# 🚀 SkyCanvas 快速启动指南

## 📋 启动前检查清单

### 1. 环境要求

请确保已安装以下软件：

- ✅ **JDK 17+** - 后端运行环境
- ✅ **Maven 3.6+** - 后端构建工具
- ✅ **MySQL 8.0+** - 数据库
- ✅ **Redis 6.0+** - 缓存
- ✅ **Node.js 14+** - 前端运行环境

### 2. 环境检查命令

```bash
# 打开PowerShell或CMD，执行以下命令检查

# 检查Java
java -version

# 检查Maven
mvn -version

# 检查MySQL
mysql --version

# 检查Redis
redis-server --version

# 检查Node.js
node -v
npm -v
```

---

## 🎯 快速启动（三步走）

### 第一步：初始化数据库

**方式一：使用脚本（推荐）**
```bash
# 双击运行
init-database.bat
```

**方式二：手动执行**
```bash
# 打开PowerShell，切换到项目目录
cd E:\workspace\cursor_code_main\SkyCanvas

# 导入数据库
mysql -u root -p < database\schema.sql
# 输入密码后回车
```

### 第二步：启动后端

**方式一：使用脚本（推荐）**
```bash
# 双击运行
start-backend.bat
```

**方式二：手动执行**
```bash
# 1. 复制配置文件
cd backend
copy src\main\resources\application-dev.yml.example src\main\resources\application-dev.yml

# 2. 编辑配置文件
# 使用记事本或VSCode打开 src\main\resources\application-dev.yml
# 修改数据库密码、Redis密码等配置

# 3. 启动Redis（新开一个命令行窗口）
redis-server

# 4. 启动后端（在backend目录）
mvn spring-boot:run
```

看到以下输出表示成功：
```
====================================
SkyCanvas Backend Started Successfully!
API地址: http://localhost:8080/api
====================================
```

### 第三步：启动前端

**方式一：使用脚本（推荐）**
```bash
# 双击运行
start-frontend.bat

# 选择启动方式：
# 1 - H5浏览器版本（推荐新手）
# 2 - 微信小程序版本
```

**方式二：手动执行**
```bash
# 1. 安装依赖
cd frontend
npm install

# 如果安装慢，使用国内镜像
npm install --registry=https://registry.npmmirror.com

# 2. 启动H5
npm run dev:h5
# 浏览器访问: http://localhost:3000

# 或启动小程序
npm run dev:mp-weixin
# 使用微信开发者工具导入: frontend\dist\dev\mp-weixin
```

---

## 🌐 访问地址

启动成功后：

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端API | http://localhost:8080/api | Spring Boot服务 |
| H5前端 | http://localhost:3000 | 浏览器访问 |
| 小程序 | 微信开发者工具 | 导入dist\dev\mp-weixin |

---

## ⚠️ 常见问题

### 1. 后端启动失败

**问题**: `Communications link failure`
```
解决: 
1. 检查MySQL是否启动: net start mysql
2. 检查数据库密码是否正确
3. 检查数据库是否已创建: mysql -u root -p
   USE skycanvas;
   SHOW TABLES;
```

**问题**: `Connection refused: connect` (Redis)
```
解决:
1. 启动Redis: redis-server
2. 或修改配置文件注释Redis相关配置（测试用）
```

### 2. 前端启动失败

**问题**: `npm install` 失败
```
解决:
1. 清除缓存: npm cache clean --force
2. 使用国内镜像: npm install --registry=https://registry.npmmirror.com
3. 或使用cnpm: npm install -g cnpm && cnpm install
```

**问题**: 页面空白或报错
```
解决:
1. 检查后端是否启动
2. 检查控制台报错信息
3. 检查网络请求是否正常（F12 - Network）
```

### 3. 数据库相关

**问题**: 表不存在
```
解决:
重新导入数据库:
mysql -u root -p
DROP DATABASE IF EXISTS skycanvas;
SOURCE database/schema.sql;
```

---

## 🔧 配置说明

### 后端配置（application-dev.yml）

最小配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/skycanvas
    username: root
    password: 你的MySQL密码  # 必填
  
  redis:
    host: localhost
    port: 6379
    password: ""  # 如果Redis有密码，填写这里
```

完整配置请查看：`backend\src\main\resources\application-dev.yml.example`

### 前端配置（可选）

如需修改API地址，编辑：`frontend\src\utils\request.js`

```javascript
const BASE_URL = 'http://localhost:8080/api'
```

---

## 📱 功能测试

### 1. 测试后端API

浏览器访问：
```
http://localhost:8080/api/credit/balance
```

预期结果：返回401未授权（说明后端正常）

### 2. 测试前端页面

H5访问：`http://localhost:3000`

预期结果：看到暗黑主题的首页

### 3. 测试完整流程（需要配置）

- [ ] 微信登录（需要配置微信AppID/Secret）
- [ ] 查看积分明细
- [ ] 提交视频生成任务（需要Sora API密钥）

---

## 📚 下一步

✅ 项目启动成功后：

1. **查看文档**
   - [API文档](docs/API.md) - 接口说明
   - [数据库文档](docs/DATABASE.md) - 表结构
   - [完整启动指南](QUICKSTART.md) - 详细说明

2. **配置API密钥**（可选）
   - 微信小程序 AppID/Secret
   - Sora API密钥
   - 阿里云OSS配置

3. **开始开发**
   - 后端代码：`backend\src\main\java\com\skycanvas`
   - 前端代码：`frontend\src\pages`

---

## 🆘 需要帮助？

- 📖 查看 [QUICKSTART.md](QUICKSTART.md) 详细文档
- 📊 查看 [COMPLETION_STATUS.md](COMPLETION_STATUS.md) 完成度报告
- 🎯 查看 [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) 项目总结

---

**🎉 祝你使用愉快！**

如有问题，请检查：
1. 环境是否正确安装
2. 服务是否正常启动
3. 配置文件是否正确
4. 端口是否被占用

