# API 响应格式规范

## 作用
统一所有 API 响应格式，便于前端处理。

## 适用范围
所有 REST API 接口。

## 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## 错误响应

```json
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

## HTTP 状态码映射

| 业务码 | HTTP状态 | 说明 |
|--------|----------|------|
| 200 | 200 | 成功 |
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未授权 |
| 403 | 403 | 禁止访问 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务器内部错误 |

## 分页响应

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

## API 路径规范

| 资源 | 路径 | 方法 | 说明 |
|------|------|------|------|
| 认证 | /api/auth/login | POST | 登录 |
| 认证 | /api/auth/register | POST | 注册 |
| 会话 | /api/conversations | GET | 获取会话列表 |
| 会话 | /api/conversations | POST | 创建会话 |
| 消息 | /api/conversations/{id}/messages | GET | 获取消息 |
| 消息 | /api/conversations/{id}/messages | POST | 发送消息 |
| Agent | /api/agents | GET | 获取Agent列表 |
| Agent | /api/agents | POST | 创建Agent |

## 安全配置

在 `SecurityConfig.java` 中配置路径权限：

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/agents").permitAll()  // 公开Agent列表
    .requestMatchers("/api/health").permitAll()
    .anyRequest().authenticated()
)
```
