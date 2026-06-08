# AgentHub 端到端测试脚本

## 测试前准备

### 1. 启动服务

```bash
# 终端1: 启动 MySQL (如果使用 Docker)
docker-compose up -d

# 终端2: 启动后端
cd backend && mvn spring-boot:run

# 终端3: 启动前端
cd frontend && npm run dev
```

### 2. 验证服务状态

```bash
# 后端 API
curl http://localhost:8080/api/agents

# 前端
curl http://localhost:5173
```

---

## 演示链路验收清单

### 链路 1: 用户注册/登录

```bash
# 注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","nickname":"Test User"}'

# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
```

**验证**: 返回 JWT token

---

### 链路 2: 创建会话

```bash
TOKEN="your_jwt_token"

# 创建单聊
curl -X POST http://localhost:8080/api/conversations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"测试会话","type":1}'

# 创建群聊
curl -X POST http://localhost:8080/api/conversations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"测试群聊","type":2}'
```

**验证**: 返回会话 ID

---

### 链路 3: 发送消息

```bash
TOKEN="your_jwt_token"
CONV_ID=1

# 发送消息
curl -X POST http://localhost:8080/api/conversations/$CONV_ID/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"你好"}'
```

**验证**: 返回消息 ID，检查数据库 message 表

---

### 链路 4: 单聊 Agent 回复

```bash
TOKEN="your_jwt_token"
CONV_ID=1

# 发送消息触发 Agent
curl -X POST http://localhost:8080/api/conversations/$CONV_ID/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"你好，请介绍一下自己"}'
```

**验证**: Agent 返回回复消息

---

### 链路 5: 群聊 @ 提及

```bash
TOKEN="your_jwt_token"
CONV_ID=2  # 群聊 ID

# @ 提及 Agent
curl -X POST http://localhost:8080/api/conversations/$CONV_ID/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"@\"Code Assistant\" 你好"}'
```

**验证**:
- 后端日志显示 "群聊消息检测到 @ 提及"
- 返回 Orchestrator 汇总结果

---

### 链路 6: @ 列表显示

**前端测试**:
1. 打开 http://localhost:5173
2. 登录账号
3. 进入群聊
4. 输入 `@` 符号
5. **预期**: 显示 Agent 列表（黄色调试框 + 红色边框列表）

---

### 链路 7: Artifact 卡片渲染

**前端测试**:
1. 发送包含代码的消息
2. **预期**: 显示代码高亮卡片

---

## 数据库验证

```bash
mysql -h 127.0.0.1 -u root -p1234567890 agenthub

# 查看用户
SELECT id, username FROM user;

# 查看 Agent
SELECT id, name, provider, is_public FROM agent;

# 查看会话
SELECT * FROM conversation;

# 查看消息
SELECT id, conversation_id, sender_type, content FROM message ORDER BY id DESC LIMIT 5;
```

---

## 常见问题排查

### 问题: @ 列表不显示

1. 检查浏览器控制台日志
2. 确认 `/api/agents` 返回数据
3. 确认 `is_public=1` 的 Agent 存在

### 问题: Agent 不回复

1. 检查后端日志
2. 确认 CustomAgentAdapter 已加载
3. 确认 conversation_participant 表有数据

### 问题: WebSocket 连接失败

1. 检查 ws://localhost:8080/ws 是否可访问
2. 确认 token 有效
3. 检查浏览器控制台 WebSocket 日志
