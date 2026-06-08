# API 设计规则

## RESTful 规范

### 资源命名
- 使用名词而非动词：`/conversations` 而非 `/getConversations`
- 复数形式：`/conversations` 而非 `/conversation`
- 小写字母：`/conversations` 而非 `/Conversations`
- 使用连字符分隔单词（可选）：`/conversation-types`

### HTTP 方法

| 方法 | 用途 | 示例 |
|------|------|------|
| GET | 查询 | `GET /conversations` |
| POST | 创建 | `POST /conversations` |
| PUT | 更新 | `PUT /conversations/123` |
| DELETE | 删除 | `DELETE /conversations/123` |

---

## 路径设计

### 标准路径
```
GET    /api/conversations           # 获取会话列表
POST   /api/conversations           # 创建会话
GET    /api/conversations/{id}      # 获取会话详情
DELETE /api/conversations/{id}      # 删除会话

GET    /api/conversations/{id}/messages    # 获取消息列表
POST   /api/conversations/{id}/messages    # 发送消息
```

### 认证路径（公开）
```
POST   /api/auth/register           # 注册
POST   /api/auth/login              # 登录
```

---

## 请求格式

### Header
```
Content-Type: application/json
Authorization: Bearer {token}        # 除公开接口外必填
```

### Body（JSON）
```json
{
  "name": "会话名称",
  "type": 1
}
```

---

## 响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "会话名称"
  }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "参数错误: name 不能为空",
  "data": null
}
```

---

## 安全配置

在 `SecurityConfig.java` 中配置路径权限：

```java
.authorizeHttpRequests(auth -> auth
    // 公开接口
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/agents").permitAll()        // Agent 列表公开
    .requestMatchers("/api/health").permitAll()

    // 需要认证的接口
    .anyRequest().authenticated()
)
```

---

## 分页规范

### 请求参数
```
GET /api/conversations?page=1&pageSize=20
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

---

## 排序规范

### 请求参数
```
GET /api/conversations?sort=createdAt,desc
```

### 支持的排序字段
- `createdAt` - 创建时间
- `updatedAt` - 更新时间
- `lastMessageAt` - 最后消息时间

---

## 版本控制

当前版本：`/api/v1`

未来版本：`/api/v2`

---

## 错误码规范

| 错误码 | HTTP状态 | 说明 |
|--------|----------|------|
| 200 | 200 | 成功 |
| 400 | 400 | 参数错误 |
| 401 | 401 | 未授权 |
| 403 | 403 | 禁止访问 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务器内部错误 |
