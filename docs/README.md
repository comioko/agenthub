# AgentHub 开发规范文档

本目录包含 AgentHub 项目的协作开发规范，旨在帮助团队成员高效协作、保证代码质量。

## 规范体系

```
docs/
├── specs/        # 功能规范（Spec）
├── skills/       # 开发技能（Skill）
└── rules/        # 开发规则（Rule）
```

---

## Specs（功能规范）

定义系统各模块的功能、接口和数据格式。

| 文档 | 说明 | 优先级 |
|------|------|--------|
| [agent-adapter-spec.md](./specs/agent-adapter-spec.md) | Agent 适配器接口规范 | P0 |
| [orchestrator-spec.md](./specs/orchestrator-spec.md) | 群聊调度工作流程 | P0 |
| [message-format-spec.md](./specs/message-format-spec.md) | 消息格式规范 | P0 |
| [artifact-card-spec.md](./specs/artifact-card-spec.md) | 富媒体卡片规范 | P1 |
| [api-response-spec.md](./specs/api-response-spec.md) | API 响应格式规范 | P1 |
| [websocket-protocol-spec.md](./specs/websocket-protocol-spec.md) | WebSocket 通信协议 | P1 |

---

## Skills（开发技能）

指导特定开发任务的执行步骤和注意事项。

| 文档 | 适用场景 |
|------|----------|
| [agent-adapter-dev.md](./skills/agent-adapter-dev.md) | 新增 Agent Provider 适配器 |
| [message-card-dev.md](./skills/message-card-dev.md) | 新增消息卡片类型 |
| [java-api-dev.md](./skills/java-api-dev.md) | Java 后端 API 开发 |
| [orchestrator-debug.md](./skills/orchestrator-debug.md) | 群聊问题排查 |
| [frontend-bug-debug.md](./skills/frontend-bug-debug.md) | 前端问题排查 |
| [demo-prep.md](./skills/demo-prep.md) | Demo 演示准备 |

---

## Rules（开发规则）

定义开发中必须遵守的规范和约定。

| 文档 | 说明 |
|------|------|
| [naming-conventions.md](./rules/naming-conventions.md) | 命名规范 |
| [file-organization.md](./rules/file-organization.md) | 文件组织规范 |
| [api-design-rules.md](./rules/api-design-rules.md) | API 设计规则 |
| [module-boundaries.md](./rules/module-boundaries.md) | 模块边界规范 |

---

## 快速索引

### 新增 Agent Provider
1. 阅读 [agent-adapter-spec.md](./specs/agent-adapter-spec.md)
2. 按照 [agent-adapter-dev.md](./skills/agent-adapter-dev.md) 执行

### 新增消息卡片
1. 阅读 [artifact-card-spec.md](./specs/artifact-card-spec.md)
2. 按照 [message-card-dev.md](./skills/message-card-dev.md) 执行

### 开发 REST API
1. 阅读 [api-design-rules.md](./rules/api-design-rules.md)
2. 按照 [java-api-dev.md](./skills/java-api-dev.md) 执行

### 排查问题
- 群聊问题 → [orchestrator-debug.md](./skills/orchestrator-debug.md)
- 前端问题 → [frontend-bug-debug.md](./skills/frontend-bug-debug.md)

---

## 规范更新

本规范采用"先实践后规范"的原则：

1. **新功能开发** → 先按直觉开发
2. **总结经验** → 发现通用模式
3. **形成规范** → 写入对应文档
4. **后续遵循** → 新功能按规范开发

如发现规范与实践不符，请提出 Issue 或直接修改文档。
