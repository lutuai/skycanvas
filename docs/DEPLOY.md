# SkyCanvas 部署文档

## 环境要求

### 后端
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 前端
- Node.js 14+
- npm 6+ / yarn 1.22+

### 云服务（阿里云）
- ECS服务器（2核4G起）
- RDS MySQL（标准版）
- Redis（标准版）
- OSS对象存储
- CDN（可选）

## 本地开发环境搭建

### 1. 数据库初始化

```bash
# 连接MySQL
mysql -u root -p

# 导入数据库脚本
source database/schema.sql
```

### 2. 后端启动

```bash
cd backend

# 复制配置文件
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml

# 修改配置文件，填写数据库、Redis、微信等配置
vim src/main/resources/application-dev.yml

# 启动项目
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 微信小程序开发
npm run dev:mp-weixin

# H5开发
npm run dev:h5
```

- 小程序：使用微信开发者工具导入 `frontend/dist/dev/mp-weixin` 目录
- H5：访问 `http://localhost:3000`

## 生产环境部署

### 一、准备工作

#### 1. 购买阿里云服务

1. **ECS服务器**
   - 配置：2核4G（入门）/ 4核8G（推荐）
   - 操作系统：CentOS 7.9 / Ubuntu 20.04
   - 网络：公网IP + 安全组配置

2. **RDS MySQL**
   - 版本：MySQL 8.0
   - 规格：基础版（入门）/ 高可用版（推荐）

3. **Redis**
   - 版本：6.0
   - 规格：标准版

4. **OSS对象存储**
   - 创建Bucket
   - 配置CDN加速域名

#### 2. 域名配置

```
- 主域名：skycanvas.com
- API域名：api.skycanvas.com
- CDN域名：cdn.skycanvas.com
- H5域名：h5.skycanvas.com
```

#### 3. SSL证书

申请并配置SSL证书（免费证书或商业证书）

### 二、后端部署

#### 1. 环境准备

```bash
# 安装JDK 17
sudo yum install java-17-openjdk-devel

# 安装Maven（可选，可本地打包后上传）
sudo yum install maven

# 创建应用目录
mkdir -p /app/skycanvas
cd /app/skycanvas
```

#### 2. 打包上传

```bash
# 本地打包
cd backend
mvn clean package -DskipTests

# 上传到服务器
scp target/skycanvas-backend-1.0.0.jar root@your-server:/app/skycanvas/
```

#### 3. 配置文件

在服务器上创建 `application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-rds-url:3306/skycanvas
    username: your_username
    password: your_password
  
  redis:
    host: your-redis-host
    port: 6379
    password: your_redis_password

wechat:
  miniapp:
    app-id: your_appid
    app-secret: your_secret

aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your_key
    access-key-secret: your_secret
    bucket-name: skycanvas

video:
  provider:
    sora-proxy:
      api-key: your_sora_api_key
```

#### 4. 启动脚本

创建 `start.sh`:

```bash
#!/bin/bash

APP_NAME=skycanvas-backend-1.0.0.jar
JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
SPRING_PROFILE=prod

nohup java $JVM_OPTS -jar $APP_NAME --spring.profiles.active=$SPRING_PROFILE > app.log 2>&1 &

echo "Application started!"
```

```bash
chmod +x start.sh
./start.sh
```

#### 5. 配置Nginx反向代理

```nginx
server {
    listen 80;
    server_name api.skycanvas.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name api.skycanvas.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 三、前端部署

#### 1. 微信小程序

```bash
cd frontend

# 修改manifest.json中的appid
# 修改API地址为生产环境

# 打包
npm run build:mp-weixin

# 使用微信开发者工具上传
# 提交审核
```

#### 2. H5部署

```bash
# 打包
npm run build:h5

# 上传到服务器
scp -r dist/build/h5/* root@your-server:/var/www/h5/

# Nginx配置
server {
    listen 80;
    server_name h5.skycanvas.com;
    root /var/www/h5;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 四、数据库部署

```bash
# 连接RDS
mysql -h your-rds-url -u username -p

# 导入数据库
source schema.sql

# 创建备份任务（阿里云控制台配置）
```

### 五、监控和日志

#### 1. 应用日志

```bash
# 查看实时日志
tail -f /app/skycanvas/app.log

# 日志轮转配置 /etc/logrotate.d/skycanvas
/app/skycanvas/app.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
}
```

#### 2. 性能监控

- 使用阿里云监控服务
- 配置告警规则（CPU、内存、磁盘）
- 配置钉钉/短信通知

#### 3. 数据库监控

- RDS慢查询日志
- 连接数监控
- 磁盘空间监控

### 六、安全加固

#### 1. 防火墙配置

```bash
# 只开放必要端口
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --reload
```

#### 2. 数据库安全

- 白名单IP访问
- 强密码策略
- 定期备份

#### 3. Redis安全

- 设置密码
- 禁用危险命令
- 内网访问

### 七、常见问题

#### 1. 服务启动失败

```bash
# 查看日志
cat /app/skycanvas/app.log

# 检查端口占用
netstat -nltp | grep 8080

# 检查Java进程
jps -l
```

#### 2. 数据库连接失败

- 检查RDS白名单
- 检查账号密码
- 检查网络连通性

#### 3. Redis连接失败

- 检查密码配置
- 检查网络连通性
- 检查Redis服务状态

### 八、升级流程

```bash
# 1. 备份数据库
mysqldump -h your-rds-url -u username -p skycanvas > backup.sql

# 2. 停止应用
kill -15 $(cat app.pid)

# 3. 备份旧版本
mv skycanvas-backend-1.0.0.jar skycanvas-backend-1.0.0.jar.bak

# 4. 上传新版本
scp target/skycanvas-backend-1.1.0.jar root@your-server:/app/skycanvas/

# 5. 启动应用
./start.sh

# 6. 验证服务
curl http://localhost:8080/api/health
```

### 九、成本估算

| 服务 | 配置 | 月费用 |
|------|------|--------|
| ECS | 2核4G | ¥200-300 |
| RDS MySQL | 基础版 | ¥300-500 |
| Redis | 标准版 | ¥200-300 |
| OSS | 100GB | ¥50-100 |
| CDN | 100GB流量 | ¥50-100 |
| **合计** | - | **¥800-1300/月** |

### 十、性能优化建议

1. **开启Redis缓存**: 用户信息、热门作品
2. **CDN加速**: 静态资源、视频文件
3. **数据库索引优化**: 根据慢查询优化
4. **应用层优化**: 异步处理、线程池配置
5. **负载均衡**: 多台ECS + SLB（用户量大时）

