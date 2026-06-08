# Message 消息格式规范

## 作用
统一前后端消息格式，确保各模块间数据一致性。

## 适用范围
Message 实体类、WebSocket 消息、API 响应。

## Message 类型

| 类型 | 值 | 说明 |
|------|---|------|
| TEXT | 1 | 普通文本消息 |
| ARTIFACT | 2 | 富媒体消息（含卡片） |
| SYSTEM | 3 | 系统消息 |

## SenderType 发送者类型

| 类型 | 值 | 说明 |
|------|---|------|
| USER | 1 | 普通用户 |
| AGENT | 2 | Agent 回复 |
| ORCHESTRATOR | 3 | 群聊汇总消息 |

## Message 结构

```java
public class Message {
    Long id;              // 消息ID
    Long conversationId;  // 会话ID
    Long senderId;       // 发送者ID
    Integer senderType;   // 发送者类型
    String content;       // 消息内容
    Integer messageType;   // 消息类型
    LocalDateTime createdAt;
    List<MessageBlock> blocks;  // 富媒体块（可选）
}
```

## 数据库表结构

```sql
CREATE TABLE message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_type TINYINT NOT NULL COMMENT '1=user, 2=agent, 3=orchestrator',
    content TEXT NOT NULL,
    message_type TINYINT NOT NULL COMMENT '1=text, 2=artifact, 3=system',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conv (conversation_id),
    INDEX idx_created (created_at)
);
```

## WebSocket 消息格式

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

## 消息发送流程

1. 用户发送消息 → 保存到数据库 → 广播到 WebSocket
2. Agent 处理 → 保存到数据库 → 广播到 WebSocket
3. 群聊汇总 → 保存到数据库 → 广播到 WebSocket

## 前端类型定义

```typescript
interface Message {
  id: number
  conversationId: number
  senderId: number
  senderType: number // 1=user, 2=agent, 3=orchestrator
  content: string
  messageType: number // 1=text, 2=artifact, 3=system
  createdAt: string
  blocks?: MessageBlock[]
}
```
