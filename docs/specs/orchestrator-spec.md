# Orchestrator 工作流程

## 作用
定义 Orchestrator 如何解析 @ 提及、分派任务、汇总结果。

## 适用范围
Orchestrator 类及相关服务。

## 流程概述

```
用户消息 → @解析 → Agent查找 → 任务分派 → 并行执行 → 结果汇总 → 返回
```

## @ 提及格式

### 支持的格式

1. **引号格式（推荐）**: `@"Agent Name"` - 支持空格
2. **无引号格式**: `@AgentName` - 不支持空格

### 示例

```
@Code Assistant 你好
@"Data Analyst" 分析这个数据
@Agent1 @"Agent With Space" 一起处理
```

## 解析规则

1. 优先匹配引号格式 `@"..."`
2. 再匹配无引号格式 `@\S+`
3. 匹配结果去重

```java
// 伪代码
List<String> parseMentions(String content) {
    // 1. 先提取 @"..." 格式
    // 2. 再提取 @xxx 格式
    // 3. 去重返回
}
```

## 任务分派规则

1. 每个 @ 提及生成一个 Task 记录
2. Task 并行执行（使用线程池，5个线程）
3. 所有 Task 完成后按提及顺序汇总
4. 单个 Agent 超时不影响其他 Agent 结果

## 结果汇总格式

```
【协作完成】

🤖 **Agent1**:
...结果1...

🤖 **Agent2**:
...结果2...
```

## 配置参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| maxConcurrentAgents | 5 | 最大并发 Agent 数 |
| agentTimeoutSeconds | 30 | 单个 Agent 超时时间 |
| retryAttempts | 1 | 失败重试次数 |

## 核心类说明

| 类 | 职责 |
|----|------|
| Orchestrator | 协调者：解析、分派、汇总 |
| AgentAdapter | 适配器接口：调用外部 Agent |
| MessageService | 消息服务：持久化 + 广播 |
| WebSocketHandler | WebSocket：实时推送 |
