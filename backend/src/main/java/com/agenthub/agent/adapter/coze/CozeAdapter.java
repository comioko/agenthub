package com.agenthub.agent.adapter.coze;

import com.agenthub.agent.adapter.AgentAdapter;
import com.agenthub.agent.core.AgentRequest;
import com.agenthub.agent.core.AgentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CozeAdapter implements AgentAdapter {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiBase;

    public CozeAdapter(
            @Value("${agent.coze.api-key}") String apiKey,
            @Value("${agent.coze.api-base}") String apiBase) {
        this.apiKey = apiKey;
        this.apiBase = apiBase;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProvider() {
        return "coze";
    }

    @Override
    public boolean supports(String agentId) {
        return agentId != null && agentId.startsWith("coze-");
    }

    @Override
    public AgentResponse chat(AgentRequest request) {
        try {
            String userMessage = extractUserMessage(request);
            String systemPrompt = request.getSystemPrompt();

            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            String jsonBody = buildRequestBody(userMessage, systemPrompt, request.getAgentId());

            RequestBody body = RequestBody.create(jsonBody, mediaType);
            Request httpRequest = new Request.Builder()
                    .url(apiBase + "/v1/chat")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    log.error("Coze API error: {} - {}", response.code(), responseBody);
                    return createErrorResponse("Coze API 调用失败: " + response.code());
                }

                return parseResponse(responseBody);
            }
        } catch (Exception e) {
            log.error("Coze chat error", e);
            return createErrorResponse("调用 Coze 出错: " + e.getMessage());
        }
    }

    private String extractUserMessage(AgentRequest request) {
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            return request.getMessages().get(request.getMessages().size() - 1).getContent();
        }
        return "";
    }

    private String buildRequestBody(String userMessage, String systemPrompt, String agentId) throws IOException {
        // Coze API 请求格式
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("conversation_id", "");
        requestBody.put("bot_id", extractBotId(agentId));

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        requestBody.put("stream", false);

        return objectMapper.writeValueAsString(requestBody);
    }

    private String extractBotId(String agentId) {
        // agentId 格式: coze-{bot_id}
        if (agentId != null && agentId.startsWith("coze-")) {
            return agentId.substring(5);
        }
        return "";
    }

    private AgentResponse parseResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);

        // Coze API 响应格式
        // {"code": 0, "msg": "success", "data": {...}}
        int code = root.path("code").asInt(0);
        if (code != 0) {
            String msg = root.path("msg").asText("Unknown error");
            return createErrorResponse("Coze API error: " + msg);
        }

        JsonNode data = root.path("data");
        if (data.isMissingNode()) {
            return createErrorResponse("Coze 返回数据格式异常");
        }

        // 从 data 中提取消息
        JsonNode messages = data.path("messages");
        if (messages.isArray()) {
            for (JsonNode msg : messages) {
                String role = msg.path("role").asText("");
                if ("assistant".equals(role)) {
                    String content = msg.path("content").asText("");
                    AgentResponse resp = new AgentResponse();
                    resp.setContent(content);
                    return resp;
                }
            }
        }

        return createErrorResponse("Coze 未返回有效消息");
    }

    private AgentResponse createErrorResponse(String message) {
        AgentResponse resp = new AgentResponse();
        resp.setContent(message);
        return resp;
    }

    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList("coze-model");
    }
}
