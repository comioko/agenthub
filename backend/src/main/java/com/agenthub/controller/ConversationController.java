package com.agenthub.controller;

import com.agenthub.model.entity.Conversation;
import com.agenthub.model.entity.Message;
import com.agenthub.service.ConversationService;
import com.agenthub.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<Conversation> createConversation(
            Authentication auth,
            @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        String name = (String) body.get("name");
        Integer type = (Integer) body.getOrDefault("type", 1);
        Long agentId = body.get("agentId") != null ? ((Number) body.get("agentId")).longValue() : null;
        return ResponseEntity.ok(conversationService.createConversation(userId, name, type, agentId));
    }

    @GetMapping
    public ResponseEntity<List<Conversation>> getConversations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }

    @GetMapping("/{convId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long convId) {
        return ResponseEntity.ok(messageService.getMessages(convId));
    }

    @PostMapping("/{convId}/messages")
    public ResponseEntity<Message> sendMessage(
            Authentication auth,
            @PathVariable Long convId,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        String content = body.get("content");
        return ResponseEntity.ok(messageService.sendMessage(userId, convId, content));
    }
}
