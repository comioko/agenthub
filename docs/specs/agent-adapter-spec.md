# Agent Adapter 接口规范

## 作用
统一所有 Agent Provider 的接入方式，确保新 Provider 可以无缝接入。

## 适用范围
所有 Agent 适配器实现类。

## 接口定义

所有 Agent 适配器必须实现 `AgentAdapter` 接口：

```java
public interface AgentAdapter {
    String getProvider();                           // 返回 Provider 名称
    boolean supports(String agentId);               // 是否支持该 Agent
    AgentResponse chat(AgentRequest request);       // 同步对话
    default List<String> getSupportedModels() {}    // 支持的模型列表
}
```

## 请求格式

```json
{
  "agentId": "minimax-123",
  "messages": [
    {"role": "system", "content": "你是一个..."},
    {"role": "user", "content": "用户消息"}
  ],
  "systemPrompt": "可选的系统提示",
  "context": {}
}
```

## 响应格式

```json
{
  "content": "Agent 回复内容",
  "blocks": [
    {
      "type": "code",
      "language": "java",
      "content": "代码内容",
      "title": "可选标题"
    }
  ],
  "reasoning": "思考过程（可选）"
}
```

## Provider 命名规则

| Provider | 命名示例 | 说明 |
|----------|---------|------|
| MiniMax | minimax-{agentId} | minimax-123 |
| Coze | coze-{agentId} | coze-456 |
| Custom | custom-{agentId} | custom-789 |
| System Default | system-default | 内置默认适配器 |

## 现有适配器

| Provider | 实现类 | 状态 |
|----------|--------|------|
| minimax | MiniMaxAdapter | 已实现 |
| custom | CustomAgentAdapter | 已实现 |
| coze | CozeAdapter | 待实现 |

## 实现 Checklist

- [ ] 实现 AgentAdapter 接口
- [ ] 添加 @Component 注解
- [ ] 在 supports() 中正确定义匹配规则
- [ ] 处理 API 超时和错误
- [ ] 编写单元测试（覆盖率 > 80%）
- [ ] 更新本文档的 Provider 列表
