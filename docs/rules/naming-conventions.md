# 命名规范

## Java 代码

### 类名
- UpperCamelCase
- 示例：`AgentService`, `MessageService`, `MiniMaxAdapter`

### 方法名
- lowerCamelCase
- 示例：`chatWithAgent`, `sendMessage`, `findAgentsByMentions`

### 常量
- UPPER_SNAKE_CASE
- 示例：`MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`

### 包名
- lowercase
- 示例：`com.agenthub.service`, `com.agenthub.agent.adapter`

### 成员变量
- lowerCamelCase
- 示例：`conversationId`, `agentList`

---

## TypeScript / React

### 组件名
- PascalCase
- 示例：`MentionAutocomplete`, `CodeCard`, `AgentList`

### 文件名
- PascalCase.tsx（组件）
- camelCase.ts（其他）
- 示例：`Chat.tsx`, `socket.ts`, `agent.ts`

### 变量和函数
- camelCase
- 示例：`handleClick`, `loadAgents`, `conversationList`

### 类型定义
- PascalCase
- 示例：`interface UserProps`, `type MessageStatus`

---

## 数据库

### 表名
- snake_case
- 示例：`conversation_participant`, `message_block`

### 字段名
- snake_case
- 示例：`created_at`, `conversation_id`, `is_public`

### 索引名
- `idx_{table}_{column}`
- 示例：`idx_conversation_id`, `idx_user_username`

### 外键
- `{table_singular}_id`
- 示例：`user_id`, `conversation_id`

---

## API 路径

### RESTful 规范
- `/api/{resource}` - 资源列表
- `/api/{resource}/{id}` - 单个资源
- `/api/{resource}/{id}/{sub}` - 子资源

### 示例
```
/api/conversations
/api/conversations/123
/api/conversations/123/messages
/api/agents
```

---

## WebSocket 消息类型

### 客户端 → 服务器
- `subscribe`
- `unsubscribe`
- `ping`

### 服务器 → 客户端
- `connected`
- `message`
- `disconnected`

---

## 消息类型枚举

### MessageType
| 类型 | 值 | 说明 |
|------|---|------|
| TEXT | 1 | 普通文本 |
| ARTIFACT | 2 | 富媒体 |
| SYSTEM | 3 | 系统消息 |

### SenderType
| 类型 | 值 | 说明 |
|------|---|------|
| USER | 1 | 用户 |
| AGENT | 2 | Agent |
| ORCHESTRATOR | 3 | 汇总 |

### ConversationType
| 类型 | 值 | 说明 |
|------|---|------|
| SINGLE | 1 | 单聊 |
| GROUP | 2 | 群聊 |

---

## Agent Provider 命名

| Provider | ID 前缀 | 示例 |
|----------|---------|------|
| MiniMax | minimax- | minimax-123 |
| Coze | coze- | coze-456 |
| Custom | custom- | custom-789 |
| System | system- | system-default |
