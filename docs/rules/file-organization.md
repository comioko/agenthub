# 文件组织规范

## 后端 (Java)

```
backend/src/main/java/com/agenthub/
├── controller/              # REST API 控制器
│   ├── AuthController.java
│   ├── ConversationController.java
│   ├── MessageController.java
│   └── AgentController.java
│
├── service/                 # 业务逻辑
│   ├── AuthService.java
│   ├── ConversationService.java
│   ├── MessageService.java
│   └── AgentService.java
│
├── repository/               # 数据访问
│   ├── UserRepository.java
│   ├── ConversationRepository.java
│   ├── ConversationParticipantRepository.java
│   ├── MessageRepository.java
│   ├── MessageBlockRepository.java
│   └── AgentRepository.java
│
├── model/
│   ├── entity/              # 数据库实体
│   │   ├── User.java
│   │   ├── Conversation.java
│   │   ├── ConversationParticipant.java
│   │   ├── Message.java
│   │   ├── MessageBlock.java
│   │   └── Agent.java
│   └── dto/                # 数据传输对象（按需添加）
│
├── agent/
│   ├── adapter/            # Agent 适配器
│   │   ├── AgentAdapter.java           # 接口
│   │   ├── minimax/                  # MiniMax 实现
│   │   │   └── MiniMaxAdapter.java
│   │   ├── coze/                     # Coze 实现（待实现）
│   │   │   └── CozeAdapter.java
│   │   └── custom/                   # Custom 实现
│   │       └── CustomAgentAdapter.java
│   ├── core/               # Agent 核心类
│   │   ├── AgentRequest.java
│   │   └── AgentResponse.java
│   └── orchestrator/      # 群聊调度
│       └── Orchestrator.java
│
├── security/               # JWT 鉴权
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
│
├── config/                # 配置类
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   ├── CorsConfig.java
│   └── MyBatisPlusConfig.java
│
└── websocket/            # WebSocket 处理
    └── WebSocketHandler.java
```

**规则**：
- Controller 只负责接收请求、调用 Service、返回响应
- Service 处理业务逻辑，不直接操作 HTTP
- Repository 只做数据访问
- Agent 模块独立，不被业务模块直接调用

---

## 前端 (React)

```
frontend/src/
├── api/                   # API 请求封装
│   ├── axios.ts          # axios 实例配置
│   ├── auth.ts           # 认证相关 API
│   ├── agent.ts          # Agent 相关 API
│   └── conversation.ts    # 会话相关 API
│
├── components/           # 公共组件
│   ├── MentionAutocomplete.tsx
│   └── Layout/
│       └── MainLayout.tsx
│
├── components/ArtifactCard/  # 富媒体卡片
│   ├── index.ts
│   ├── CodeCard.tsx
│   ├── DiffCard.tsx
│   ├── WebPreviewCard.tsx   # 待实现
│   ├── FileCard.tsx         # 待实现
│   └── DeployCard.tsx       # 待实现
│
├── pages/                # 页面组件
│   ├── Login.tsx
│   ├── Chat.tsx
│   └── AgentList.tsx
│
├── stores/               # 状态管理（Zustand）
│   └── authStore.ts
│
├── types/               # TypeScript 类型
│   └── index.ts
│
├── hooks/               # 自定义 Hooks
│   └── useAuth.ts
│
├── utils/               # 工具函数
│   └── format.ts
│
├── websocket/           # WebSocket 封装
│   └── socket.ts
│
├── App.tsx
└── main.tsx
```

**规则**：
- 页面组件放在 `pages/`
- 可复用组件放在 `components/`
- 卡片组件放在 `components/ArtifactCard/`
- API 封装统一放在 `api/`

---

## 资源文件

```
backend/src/main/resources/
├── application.yml       # 主配置
├── schema.sql           # 数据库建表脚本
└── mapper/              # MyBatis XML（按需）
    └── UserMapper.xml
```

---

## 文档目录

```
docs/
├── specs/               # Spec 规范
│   ├── agent-adapter-spec.md
│   ├── orchestrator-spec.md
│   ├── message-format-spec.md
│   ├── artifact-card-spec.md
│   ├── api-response-spec.md
│   └── websocket-protocol-spec.md
│
├── skills/              # Skills 指南
│   ├── agent-adapter-dev.md
│   ├── message-card-dev.md
│   ├── java-api-dev.md
│   ├── orchestrator-debug.md
│   ├── frontend-bug-debug.md
│   └── demo-prep.md
│
├── rules/               # Rules 规范
│   ├── naming-conventions.md
│   ├── file-organization.md
│   ├── api-design-rules.md
│   └── module-boundaries.md
│
└── README.md            # 规范索引
```
