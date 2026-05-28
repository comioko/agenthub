package com.agenthub.agent.adapter.minimax;

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
public class MiniMaxAdapter implements AgentAdapter {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiBase;

    public MiniMaxAdapter(
            @Value("${agent.minimax.api-key}") String apiKey,
            @Value("${agent.minimax.api-base}") String apiBase) {
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
        return "minimax";
    }

    @Override
    public boolean supports(String agentId) {
        return agentId != null && agentId.startsWith("minimax-");
    }

    @Override
    public AgentResponse chat(AgentRequest request) {
        try {
            String userMessage = extractUserMessage(request);
            String systemPrompt = request.getSystemPrompt();

            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            String jsonBody = buildRequestBody(userMessage, systemPrompt);

            RequestBody body = RequestBody.create(jsonBody, mediaType);
            Request httpRequest = new Request.Builder()
                    .url(apiBase + "/v1/text/chatcompletion_v2")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    log.error("MiniMax API error: {} - {}", response.code(), responseBody);
                    return createErrorResponse("MiniMax API 调用失败: " + response.code());
                }

                return parseResponse(responseBody);
            }
        } catch (Exception e) {
            log.error("MiniMax chat error", e);
            return createErrorResponse("调用 MiniMax 出错: " + e.getMessage());
        }
    }

    private String extractUserMessage(AgentRequest request) {
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            return request.getMessages().get(request.getMessages().size() - 1).getContent();
        }
        return "";
    }

    private String buildRequestBody(String userMessage, String systemPrompt) throws IOException {
        String prompt = systemPrompt != null ? systemPrompt : "你是 AgentHub 的 AI 助手。";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "MiniMax-Text-01");
        requestBody.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", prompt));
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        return objectMapper.writeValueAsString(requestBody);
    }

    private AgentResponse parseResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");

        if (choices.isArray() && choices.size() > 0) {
            String content = choices.get(0).path("message").path("content").asText();
            AgentResponse resp = new AgentResponse();
            resp.setContent(content);
            return resp;
        }

        return createErrorResponse("MiniMax 返回格式异常");
    }

    private AgentResponse createErrorResponse(String message) {
        AgentResponse resp = new AgentResponse();
        resp.setContent(message);
        return resp;
    }

    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList("MiniMax-Text-01");
    }
}
