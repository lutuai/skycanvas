# SkyCanvas 数据库设计文档

## 数据库概览

- **数据库名称**: skycanvas
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB

## 数据表列表

1. `tb_user` - 用户表
2. `tb_credit_log` - 积分记录表
3. `tb_video_task` - 视频生成任务表
4. `tb_work` - 作品表
5. `tb_post` - 社区帖子表
6. `tb_comment` - 评论表
7. `tb_like` - 点赞表
8. `tb_order` - 订单表
9. `tb_credit_package` - 积分套餐表

## 详细表结构

### 1. 用户表 (tb_user)

存储用户基本信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 用户ID（主键） |
| openid | VARCHAR(100) | 微信openid（唯一） |
| unionid | VARCHAR(100) | 微信unionid |
| nickname | VARCHAR(50) | 用户昵称 |
| avatar | VARCHAR(255) | 用户头像 |
| phone | VARCHAR(20) | 手机号 |
| credits | INT | 积分余额 |
| total_videos | INT | 总生成视频数 |
| status | TINYINT | 用户状态（0-正常，1-禁用） |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (openid)
- KEY (phone)
- KEY (create_time)

### 2. 积分记录表 (tb_credit_log)

记录所有积分变动

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 记录ID（主键） |
| user_id | BIGINT | 用户ID |
| amount | INT | 变动金额（正-充值，负-消费） |
| type | TINYINT | 类型（1-充值，2-消费，3-退款） |
| balance | INT | 操作后余额 |
| description | VARCHAR(200) | 描述 |
| order_id | BIGINT | 关联订单ID |
| task_id | BIGINT | 关联任务ID |
| create_time | DATETIME | 创建时间 |

**索引**:
- PRIMARY KEY (id)
- KEY (user_id)
- KEY (create_time)

### 3. 视频生成任务表 (tb_video_task)

记录所有视频生成任务

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 任务ID（主键） |
| user_id | BIGINT | 用户ID |
| task_id | VARCHAR(100) | 第三方任务ID |
| provider | VARCHAR(50) | 服务提供商 |
| prompt | TEXT | 提示词 |
| params | TEXT | 生成参数（JSON） |
| status | TINYINT | 任务状态（0-队列中，1-生成中，2-成功，3-失败） |
| progress | INT | 进度（0-100） |
| video_url | VARCHAR(500) | 视频URL |
| cover_url | VARCHAR(500) | 封面URL |
| duration | INT | 视频时长（秒） |
| cost_credits | INT | 消耗积分 |
| error_msg | VARCHAR(500) | 错误信息 |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |
| complete_time | DATETIME | 完成时间 |

**索引**:
- PRIMARY KEY (id)
- KEY (user_id)
- KEY (task_id)
- KEY (status)
- KEY (create_time)

### 4. 作品表 (tb_work)

用户的视频作品

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 作品ID（主键） |
| user_id | BIGINT | 用户ID |
| video_task_id | BIGINT | 关联视频任务ID |
| title | VARCHAR(100) | 作品标题 |
| description | VARCHAR(500) | 作品描述 |
| tags | VARCHAR(200) | 标签（JSON数组） |
| cover_url | VARCHAR(500) | 封面URL |
| video_url | VARCHAR(500) | 视频URL |
| duration | INT | 视频时长（秒） |
| is_public | TINYINT | 是否公开（0-私密，1-公开） |
| view_count | INT | 浏览次数 |
| like_count | INT | 点赞次数 |
| comment_count | INT | 评论次数 |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**索引**:
- PRIMARY KEY (id)
- KEY (user_id)
- KEY (video_task_id)
- KEY (is_public)
- KEY (create_time)

### 5. 订单表 (tb_order)

积分充值订单

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 订单ID（主键） |
| user_id | BIGINT | 用户ID |
| order_no | VARCHAR(50) | 订单号（唯一） |
| package_id | BIGINT | 套餐ID |
| amount | DECIMAL(10,2) | 订单金额（元） |
| credits | INT | 积分数量 |
| status | TINYINT | 订单状态（0-待支付，1-已支付，2-已取消，3-已退款） |
| pay_method | TINYINT | 支付方式（1-微信支付） |
| transaction_id | VARCHAR(100) | 第三方交易号 |
| pay_time | DATETIME | 支付时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (order_no)
- KEY (user_id)
- KEY (status)
- KEY (create_time)

### 6. 积分套餐表 (tb_credit_package)

积分充值套餐配置

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 套餐ID（主键） |
| name | VARCHAR(50) | 套餐名称 |
| credits | INT | 积分数量 |
| price | DECIMAL(10,2) | 价格（元） |
| bonus_credits | INT | 赠送积分 |
| sort | INT | 排序 |
| is_active | TINYINT | 是否启用（0-禁用，1-启用） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**索引**:
- PRIMARY KEY (id)
- KEY (is_active)
- KEY (sort)

## 初始化数据

### 默认积分套餐

| ID | 名称 | 积分 | 价格 | 赠送 | 排序 |
|----|------|------|------|------|------|
| 1 | 体验套餐 | 100 | ¥10 | 0 | 1 |
| 2 | 进阶套餐 | 300 | ¥30 | 20 | 2 |
| 3 | 热门套餐 | 600 | ¥50 | 100 | 3 |
| 4 | 超值套餐 | 1200 | ¥100 | 300 | 4 |

## 数据库优化建议

1. **分区策略**: 可按时间对大表（如tb_video_task、tb_credit_log）进行分区
2. **索引优化**: 根据实际查询情况添加组合索引
3. **读写分离**: 使用主从复制实现读写分离
4. **缓存策略**: 热点数据（用户信息、积分余额）使用Redis缓存
5. **归档策略**: 定期归档历史数据

## 备份策略

- **全量备份**: 每天凌晨3点进行全量备份
- **增量备份**: 每小时进行一次增量备份
- **保留策略**: 保留最近30天的备份

## 维护SQL

### 查询用户积分余额
```sql
SELECT id, nickname, credits FROM tb_user WHERE id = ?;
```

### 查询用户积分变动记录
```sql
SELECT * FROM tb_credit_log 
WHERE user_id = ? 
ORDER BY create_time DESC 
LIMIT 20;
```

### 查询用户生成任务
```sql
SELECT * FROM tb_video_task 
WHERE user_id = ? AND deleted = 0 
ORDER BY create_time DESC 
LIMIT 10;
```

### 统计今日生成任务数
```sql
SELECT COUNT(*) FROM tb_video_task 
WHERE DATE(create_time) = CURDATE();
```

### 统计积分消耗情况
```sql
SELECT DATE(create_time) as date, SUM(ABS(amount)) as total 
FROM tb_credit_log 
WHERE type = 2 
GROUP BY DATE(create_time) 
ORDER BY date DESC 
LIMIT 30;
```

