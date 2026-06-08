# WebSocket 通信协议规范

## 作用
定义 WebSocket 消息类型和通信格式。

## 适用范围
WebSocketHandler、WebSocket 客户端、前端 socket 封装。

## 连接

```
ws://localhost:8080/ws?token={jwt_token}
```

## 消息格式

所有消息均为 JSON 格式：

```json
{
  "type": "message_type",
  ...其他字段
}
```

## 客户端 → 服务器

### 1. 订阅会话

```json
{
  "type": "subscribe",
  "conversationId": 123
}
```

### 2. 取消订阅

```json
{
  "type": "unsubscribe",
  "conversationId": 123
}
```

### 3. 心跳

```json
{
  "type": "ping"
}
```

响应：
```json
{
  "type": "pong"
}
```

## 服务器 → 客户端

### 1. 连接成功

```json
{
  "type": "connected",
  "userId": 1
}
```

### 2. 新消息

```json
{
  "type": "message",
  "conversationId": 123,
  "message": {
    "id": 456,
    "senderId": 1,
    "senderType": 2,
    "content": "这是回复",
    "messageType": 1,
    "createdAt": "2026-05-29T10:00:00"
  }
}
```

### 3. 断开连接

```json
{
  "type": "disconnected",
  "status": "disconnected"
}
```

## 重连策略

| 参数 | 值 | 说明 |
|------|---|------|
| 首次重连延迟 | 1秒 | 首次失败后等待时间 |
| 最大重连次数 | 5 | 超过后停止 |
| 重连间隔 | 3秒递增 | 每次失败后增加 |

## 前端使用示例

```typescript
import { useWebSocket } from '../websocket/socket'

const { subscribe, unsubscribe } = useWebSocket(handleMessage)

// 订阅会话
subscribe(conversationId)

// 取消订阅
unsubscribe(conversationId)
```

## 后端广播示例

```java
@Autowired
private WebSocketHandler webSocketHandler;

// 广播消息
webSocketHandler.broadcastToConversation(convId, payload);
```
