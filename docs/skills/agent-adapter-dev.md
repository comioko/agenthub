# Skill: 新增 Agent Provider 适配器

## 适用场景
需要接入新的 Agent 平台（如 Coze、OpenAI、Dify）

## 输入
- Provider 名称
- API 文档（endpoint、认证方式、请求/响应格式）
- 认证方式（API Key / OAuth）

## 输出
实现 `AgentAdapter` 接口的完整适配器类

## 执行步骤

### 1. 创建目录结构

```bash
backend/src/main/java/com/agenthub/agent/adapter/{provider}/
```

### 2. 创建适配器类

```java
package com.agenthub.agent.adapter.{provider};

@Component
@Slf4j
public class {Provider}Adapter implements AgentAdapter {

    @Override
    public String getProvider() {
        return "{provider}";
    }

    @Override
    public boolean supports(String agentId) {
        return agentId != null && agentId.startsWith("{provider}-");
    }

    @Override
    public AgentResponse chat(AgentRequest request) {
        // 1. 构建请求
        // 2. 调用 API
        // 3. 解析响应
        // 4. 返回 AgentResponse
    }
}
```

### 3. 添加配置

在 `application.yml` 中添加：

```yaml
agent:
  {provider}:
    api-key: ${PROVIDER_API_KEY:your-api-key}
    api-base: ${PROVIDER_API_BASE:https://api.provider.com}
```

### 4. 编写单元测试

```java
@Test
void testChat() {
    // given
    AgentRequest request = new AgentRequest();
    request.setAgentId("{provider}-test");

    // when
    AgentResponse response = adapter.chat(request);

    // then
    assertNotNull(response.getContent());
}
```

### 5. 更新文档

- 更新 `agent-adapter-spec.md` 的 Provider 列表
- 更新 `README.md` 的技术栈说明

## 注意事项

1. **必须实现 `supports()` 方法**，正确匹配 `agentId` 前缀
2. **必须处理 API 超时**（建议 30s）
3. **错误响应要有明确的错误信息**
4. **API Key 通过环境变量注入**，不硬编码
5. **添加 `@Slf4j` 记录日志**
