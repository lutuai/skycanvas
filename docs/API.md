# SkyCanvas API文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT Token (Header: `Authorization: Bearer {token}`)
- **返回格式**: JSON

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1699999999999
}
```

## 1. 认证接口

### 1.1 微信登录

**接口**: `POST /auth/login`

**请求参数**:
```json
{
  "code": "微信登录code",
  "nickname": "用户昵称（可选）",
  "avatar": "用户头像（可选）"
}
```

**响应**:
```json
{
  "id": 1,
  "nickname": "用户昵称",
  "avatar": "头像URL",
  "credits": 100,
  "totalVideos": 5,
  "token": "jwt_token"
}
```

### 1.2 获取用户信息

**接口**: `GET /auth/userinfo`

**Headers**: 需要认证

**响应**: 同登录接口

## 2. 视频生成接口

### 2.1 提交生成任务

**接口**: `POST /video/generate`

**Headers**: 需要认证

**请求参数**:
```json
{
  "prompt": "一个美女在直播间介绍身上的衣服",
  "imageUrl": "图片URL（可选）",
  "duration": 10,
  "resolution": "720p",
  "style": "realistic",
  "aspectRatio": "landscape",
  "temperature": 0.7
}
```

**响应**:
```json
{
  "taskId": "task_xxx",
  "status": "PENDING",
  "progress": 0,
  "estimatedTime": 300
}
```

### 2.2 查询任务状态

**接口**: `GET /video/task/{taskId}`

**Headers**: 需要认证

**响应**:
```json
{
  "taskId": "task_xxx",
  "status": "COMPLETED",
  "progress": 100,
  "videoUrl": "https://...",
  "coverUrl": "https://...",
  "duration": 10
}
```

**状态说明**:
- `PENDING`: 排队中
- `PROCESSING`: 生成中
- `COMPLETED`: 已完成
- `FAILED`: 失败

### 2.3 获取任务列表

**接口**: `GET /video/tasks`

**Headers**: 需要认证

**请求参数**:
- `current`: 当前页（默认1）
- `size`: 每页数量（默认10）

**响应**:
```json
{
  "records": [],
  "total": 100,
  "current": 1,
  "size": 10,
  "pages": 10
}
```

## 3. 积分接口

### 3.1 获取积分余额

**接口**: `GET /credit/balance`

**Headers**: 需要认证

**响应**: `100` (整数)

### 3.2 获取积分记录

**接口**: `GET /credit/logs`

**Headers**: 需要认证

**请求参数**:
- `current`: 当前页（默认1）
- `size`: 每页数量（默认20）

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "amount": -10,
      "type": 2,
      "balance": 90,
      "description": "生成10秒视频",
      "createTime": "2024-01-01 12:00:00"
    }
  ],
  "total": 50,
  "current": 1,
  "size": 20,
  "pages": 3
}
```

**类型说明**:
- `1`: 充值
- `2`: 消费
- `3`: 退款

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限 |
| 500 | 服务器错误 |

## 调用示例

### JavaScript (uni-app)

```javascript
import { request } from '@/utils/request'

// 生成视频
const result = await request({
  url: '/video/generate',
  method: 'POST',
  data: {
    prompt: '一个美女在直播间介绍身上的衣服',
    duration: 10,
    resolution: '720p'
  }
})
```

### cURL

```bash
curl -X POST http://localhost:8080/api/video/generate \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "一个美女在直播间介绍身上的衣服",
    "duration": 10,
    "resolution": "720p"
  }'
```

