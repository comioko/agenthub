package com.agenthub.agent.adapter.custom;

import com.agenthub.agent.adapter.AgentAdapter;
import com.agenthub.agent.core.AgentRequest;
import com.agenthub.agent.core.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义 Agent 适配器（MVP 版本使用规则响应）
 * 后续可扩展为调用本地 LLM 或其他自定义逻辑
 */
@Component
@Slf4j
public class CustomAgentAdapter implements AgentAdapter {

    @Override
    public String getProvider() {
        return "custom";
    }

    @Override
    public boolean supports(String agentId) {
        return agentId != null && (agentId.startsWith("custom-") || agentId.equals("system-default"));
    }

    @Override
    public AgentResponse chat(AgentRequest request) {
        log.info("CustomAgent processing request for agentId: {}", request.getAgentId());

        String userMessage = "";
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            userMessage = request.getMessages().get(request.getMessages().size() - 1).getContent();
        }

        // MVP: 简单的规则响应
        String response = generateResponse(userMessage);

        AgentResponse resp = new AgentResponse();
        resp.setContent(response);
        return resp;
    }

    private String generateResponse(String userMessage) {
        if (userMessage.contains("@")) {
            return "我收到了你的消息。在群聊中，我负责协调多个 Agent 协作。由于这是 MVP 阶段，群聊功能正在完善中。";
        }

        return "你好！我是 AgentHub 的 AI 助手。\n\n" +
               "我已收到你的消息：\n" + userMessage + "\n\n" +
               "在 MVP 版本中，我可以进行简单的单聊对话。\n" +
               "群聊协作、代码执行等高级功能正在开发中。";
    }
}
