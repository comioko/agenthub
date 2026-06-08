# Skill: Demo 演示准备

## 适用场景
演示前检查所有功能链路、确保演示流畅

## 演示前检查清单

### 1. 数据库准备

```bash
# 检查测试用户
mysql -h 127.0.0.1 -u root -p1234567890 agenthub -e \
  "SELECT id, username FROM user LIMIT 3"

# 检查 Agent 数据
mysql -h 127.0.0.1 -u root -p1234567890 agenthub -e \
  "SELECT id, name, provider, is_public FROM agent"

# 检查会话数据（可选清理旧数据）
mysql -h 127.0.0.1 -u root -p1234567890 agenthub -e \
  "DELETE FROM message WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY)"
```

### 2. 后端服务检查

```bash
# 检查 Spring Boot 启动
curl http://localhost:8080/api/health
# 期望返回: {"status":"UP"}

# 检查 Agent API
curl http://localhost:8080/api/agents
# 期望返回: Agent 列表 JSON

# 检查日志
tail -f backend/logs/spring.log
```

### 3. 前端服务检查

```bash
# 检查前端启动
curl http://localhost:5173
# 期望返回: HTML 页面

# 检查代理是否正确
# vite.config.ts 中配置 /api -> http://localhost:8080/api
```

### 4. 核心链路测试

#### 单聊链路
```bash
# 1. 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 2. 创建会话
curl -X POST http://localhost:8080/api/conversations \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试会话","type":1}'

# 3. 发送消息
curl -X POST http://localhost:8080/api/conversations/1/messages \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"content":"你好"}'
```

#### 群聊链路
```bash
# 1. 创建群聊
curl -X POST http://localhost:8080/api/conversations \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试群聊","type":2}'

# 2. 发送 @ 消息
curl -X POST http://localhost:8080/api/conversations/2/messages \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"content":"@\"Code Assistant\" 你好"}'
```

### 5. 演示环境清理

```bash
# 清理旧会话
mysql -h 127.0.0.1 -u root -p1234567890 agenthub -e \
  "DELETE FROM conversation WHERE name LIKE '%测试%'"

# 清理旧消息
mysql -h 127.0.0.1 -u root -p1234567890 agenthub -e \
  "DELETE FROM message WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 DAY)"

# 重置测试用户（可选）
# 确保有 1-2 个可用测试账号
```

## 演示脚本模板

```markdown
# AgentHub Demo 脚本

## 1. 开场（1分钟）
- 介绍 AgentHub：多 Agent 协作平台
- 核心能力：单聊、群聊、Orchestrator 调度

## 2. 单聊演示（2分钟）
- 登录账号
- 创建与 Agent 的单聊
- 发送消息，Agent 回复

## 3. 群聊演示（3分钟）
- 创建群聊
- @ 提及多个 Agent
- 展示并行调用和结果汇总

## 4. 富媒体卡片（1分钟）
- 展示代码块卡片
- 展示差异对比卡片（如有）

## 5. 结束（1分钟）
- 总结核心功能
- 演示后续规划
```

## 备用方案

### 如 MiniMax API 不可用
使用 CustomAgentAdapter 作为备用：
- 在 application.yml 中确保 `agent.minimax.api-key` 为默认值
- CustomAgentAdapter 会自动作为 fallback

### 如 WebSocket 连接失败
刷新页面重试，或检查：
- 后端 WebSocket 配置
- 前端 ws:// URL 配置
- 防火墙设置

### 如数据库连接失败
检查：
- MySQL 容器是否运行：`docker ps`
- 连接信息是否正确
- 用户权限是否足够
