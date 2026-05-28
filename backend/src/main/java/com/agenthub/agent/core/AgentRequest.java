package com.agenthub.agent.core;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AgentRequest {
    private String agentId;
    private List<ChatMessage> messages;
    private String systemPrompt;
    private Map<String, Object> context;

    @Data
    public static class ChatMessage {
        private String role; // user/assistant/system
        private String content;
    }
}
