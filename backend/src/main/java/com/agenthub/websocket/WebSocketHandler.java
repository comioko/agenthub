package com.agenthub.websocket;

import com.agenthub.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 用户 WebSocket 会话缓存 (userId -> session)
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // 会话订阅者 (conversationId -> set of userIds)
    private final Map<Long, Map<Long, WebSocketSession>> conversationSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 URL 参数获取 token
        String query = session.getUri().getQuery();
        String token = extractToken(query);

        if (token == null || !jwtUtil.validateToken(token)) {
            session.close(CloseStatus.POLICY_VIOLATION);
            log.warn("WebSocket 连接失败: 无效的 token");
            return;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        userSessions.put(userId, session);
        log.info("WebSocket 连接建立: userId={}", userId);

        // 发送欢迎消息
        sendMessage(session, Map.of(
                "type", "connected",
                "userId", userId
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) data.get("type");

            switch (type) {
                case "subscribe":
                    handleSubscribe(session, data);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, data);
                    break;
                case "ping":
                    sendMessage(session, Map.of("type", "pong"));
                    break;
                default:
                    log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息出错", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除用户会话
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
        // 移除所有订阅
        conversationSubscriptions.values().forEach(map -> map.values().remove(session));
        log.info("WebSocket 连接关闭: {}", status);
    }

    /**
     * 订阅会话消息
     */
    private void handleSubscribe(WebSocketSession session, Map<String, Object> data) {
        Long convId = ((Number) data.get("conversationId")).longValue();
        conversationSubscriptions
                .computeIfAbsent(convId, k -> new ConcurrentHashMap<>())
                .put(getUserId(session), session);
        log.info("用户订阅会话: convId={}", convId);
    }

    /**
     * 取消订阅会话
     */
    private void handleUnsubscribe(WebSocketSession session, Map<String, Object> data) {
        Long convId = ((Number) data.get("conversationId")).longValue();
        Map<Long, WebSocketSession> subscribers = conversationSubscriptions.get(convId);
        if (subscribers != null) {
            subscribers.remove(getUserId(session));
        }
    }

    /**
     * 广播消息到会话订阅者
     */
    public void broadcastToConversation(Long convId, Object message) {
        Map<Long, WebSocketSession> subscribers = conversationSubscriptions.get(convId);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化广播消息失败", e);
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        for (WebSocketSession session : subscribers.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("发送 WebSocket 消息失败", e);
                }
            }
        }
    }

    /**
     * 发送消息到指定用户
     */
    public void sendToUser(Long userId, Object message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
            } catch (Exception e) {
                log.error("发送消息到用户失败: userId={}", userId, e);
            }
        }
    }

    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

    private Long getUserId(WebSocketSession session) {
        return userSessions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(session))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private String extractToken(String query) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }
}
