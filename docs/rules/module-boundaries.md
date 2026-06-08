# 模块边界规范

## 模块职责划分

### 业务模块 (service/)

**职责**：
- 处理业务逻辑
- 事务管理
- 调用 Agent 模块
- 不直接操作 HTTP 或 WebSocket

**包含**：
- `ConversationService` - 会话业务
- `MessageService` - 消息业务
- `UserService` - 用户业务
- `AgentService` - Agent 编排（边界类）

### Agent 模块 (agent/)

**职责**：
- 定义 Agent 适配器接口
- 实现各 Provider 适配器
- Orchestrator 任务调度
- 不处理业务逻辑

**包含**：
- `AgentAdapter` - 接口定义
- `MiniMaxAdapter` - MiniMax 实现
- `CustomAgentAdapter` - Custom 实现
- `Orchestrator` - 群聊调度

### WebSocket 模块 (websocket/)

**职责**：
- 处理 WebSocket 连接
- 管理订阅关系
- 广播消息

**包含**：
- `WebSocketHandler` - WebSocket 处理

---

## 模块调用关系

```
Controller
    ↓
Service (业务逻辑)
    ↓
┌───────────┬───────────┬───────────┐
│ Repository│ Agent     │ Other     │
│ (数据访问)│ Service   │ Service   │
└───────────┴───────────┴───────────┘
    ↓
┌─────────────────┐
│ Agent Adapter   │ ← Agent 模块
│ (外部 Agent 调用)│
└─────────────────┘
```

---

## 调用规则

### 业务模块调用 Agent

**允许**：
```java
// MessageService 调用 AgentService
@Autowired
private AgentService agentService;

public void sendMessage(...) {
    AgentResponse response = agentService.chatWithAgent(convId, content);
    // 处理响应
}
```

**不允许**：
```java
// 业务模块直接调用 Adapter
@Autowired
private List<AgentAdapter> adapters;  // ❌ 不允许

// 应该通过 AgentService
```

### Orchestrator 的边界

**Orchestrator 负责**：
- 解析 @ 提及
- 查找 Agent
- 并行调用
- 结果汇总

**Orchestrator 不负责**：
- 消息持久化（由 MessageService 处理）
- WebSocket 广播（由 WebSocketHandler 处理）
- 会话管理（由 ConversationService 处理）

---

## 前后端边界

### 后端职责

| 职责 | 说明 |
|------|------|
| 业务逻辑 | 所有业务处理在后端 |
| 数据持久化 | MySQL 数据库 |
| 认证授权 | JWT Token 验证 |
| Agent 调用 | 调用外部 API |
| 实时推送 | WebSocket 广播 |

### 前端职责

| 职责 | 说明 |
|------|------|
| UI 渲染 | React 组件 |
| 状态管理 | useState, Zustand |
| 用户交互 | 表单、按钮等 |
| API 调用 | axios 请求 |
| WebSocket 客户端 | 消息订阅 |

### 禁止事项

- ❌ 前端不能直接操作数据库
- ❌ 前端不能包含业务逻辑
- ❌ 后端不能渲染 UI
- ❌ 业务模块不能直接调用外部 API（通过 Agent 模块）

---

## 配置管理

### 开发环境
- 使用 `application.yml` 中的默认配置
- API Key 使用占位符：`${MINIMAX_API_KEY:your-key}`

### 生产环境
- 通过环境变量注入真实配置
- 不提交敏感信息到代码仓库

### Mock 切换
- 在 `application.yml` 中切换 `agent.minimax.enabled`
- 或使用 `CustomAgentAdapter` 作为备用
