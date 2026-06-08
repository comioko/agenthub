# Skill: 群聊 Orchestrator 问题排查

## 适用场景
@ 提及不工作、任务分派错误、结果汇总异常

## 输入
- 问题描述
- 相关日志
- 复现步骤

## 排查步骤

### Step 1: 检查 @ 解析

位置：`Orchestrator.parseMentions()`

```java
// 在 Chat.tsx 的 handleInputChange 中添加日志
console.log('[Chat] mentions:', mentions)

// 在 Orchestrator.parseMentions() 中添加日志
log.debug("解析到 mentions: {}", mentions);
```

**检查点**：
- 确认正则表达式匹配正确
- 确认引号格式优先解析
- 确认去重逻辑正确

### Step 2: 检查 Agent 查找

位置：`Orchestrator.findAgentsByMentions()`

```java
log.debug("查找 Agents: {}", agentNames);
List<Agent> agents = agentRepository.selectList(...);
log.debug("找到 Agents: {}", agents);
```

**检查点**：
- 确认数据库中有对应 Agent
- 确认 `is_public = true`
- 确认名称完全匹配

### Step 3: 检查任务分派

位置：`Orchestrator.processGroupChat()`

```java
log.info("群聊任务分派给 {} 个 Agent", agents.size());

// 检查并行执行
List<CompletableFuture<AgentResult>> futures = agents.stream()
    .map(agent -> CompletableFuture.supplyAsync(() -> {
        log.debug("开始调用 Agent: {}", agent.getName());
        // ...
    }, executor))
    .collect(Collectors.toList());
```

**检查点**：
- 确认线程池配置（5个线程）
- 确认并行执行逻辑
- 确认超时处理

### Step 4: 检查结果汇总

位置：`Orchestrator.processGroupChat()` 末尾

```java
StringBuilder summary = new StringBuilder();
summary.append("【协作完成】\n\n");
for (CompletableFuture<AgentResult> future : futures) {
    AgentResult result = future.join();
    summary.append("🤖 **").append(result.agentName).append("**:\n");
    summary.append(result.response).append("\n\n");
}
```

**检查点**：
- 确认拼接格式正确
- 确认 emoji 显示正常
- 确认 Markdown 格式正确

## 快速检查清单

- [ ] 后端日志级别是否为 DEBUG
- [ ] 数据库 agent 表是否有数据
- [ ] agent.is_public 是否为 true
- [ ] WebSocket 是否正常连接
- [ ] 前端 @ 列表是否能正常加载

## 常用调试命令

```bash
# 查看所有 Agent
curl http://localhost:8080/api/agents

# 查看会话关联的 Agent
mysql -h 127.0.0.1 -u root -p1234567890 agenthub -e \
  "SELECT * FROM conversation_participant WHERE agent_id IS NOT NULL"

# 测试 @ 解析
curl -X POST http://localhost:8080/api/test/parse \
  -H "Content-Type: application/json" \
  -d '{"content":"@\"Code Assistant\" 你好"}'
```
