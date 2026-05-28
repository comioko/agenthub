package com.agenthub.service;

import com.agenthub.agent.orchestrator.Orchestrator;
import com.agenthub.model.entity.Conversation;
import com.agenthub.model.entity.Message;
import com.agenthub.model.entity.MessageBlock;
import com.agenthub.repository.ConversationRepository;
import com.agenthub.repository.MessageBlockRepository;
import com.agenthub.repository.MessageRepository;
import com.agenthub.websocket.WebSocketHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageBlockRepository messageBlockRepository;
    private final ConversationRepository conversationRepository;
    private final AgentService agentService;
    private final ConversationService conversationService;
    private final Orchestrator orchestrator;
    private final WebSocketHandler webSocketHandler;

    @Transactional
    public Message sendMessage(Long userId, Long convId, String content) {
        Conversation conv = conversationRepository.selectById(convId);
        if (conv == null) {
            throw new RuntimeException("会话不存在");
        }

        // 1. 保存用户消息
        Message message = new Message();
        message.setConversationId(convId);
        message.setSenderId(userId);
        message.setSenderType(1); // user
        message.setContent(content);
        message.setMessageType(1); // text
        messageRepository.insert(message);

        conversationService.updateLastMessageTime(convId);

        // 广播用户消息
        broadcastMessage(convId, message);

        // 2. 检查是否是群聊且包含 @ 提及
        List<String> mentions = orchestrator.parseMentions(content);
        if (!mentions.isEmpty() && conv.getType() == 2) {
            // 群聊处理
            return handleGroupChat(convId, userId, content, mentions);
        }

        // 3. 单聊：调用 Agent 获取回复
        String agentResponse = agentService.chat(convId, content);

        Message agentMsg = new Message();
        agentMsg.setConversationId(convId);
        agentMsg.setSenderId(2L); // system agent
        agentMsg.setSenderType(2); // agent
        agentMsg.setContent(agentResponse);
        agentMsg.setMessageType(1);
        messageRepository.insert(agentMsg);

        conversationService.updateLastMessageTime(convId);

        // 广播 Agent 回复
        broadcastMessage(convId, agentMsg);

        return message;
    }

    /**
     * 处理群聊消息
     */
    private Message handleGroupChat(Long convId, Long userId, String content, List<String> mentions) {
        log.info("群聊消息检测到 @ 提及: {}", mentions);

        // 查找被提及的 Agent
        List<com.agenthub.model.entity.Agent> agents = orchestrator.findAgentsByMentions(mentions);

        if (agents.isEmpty()) {
            // 没有找到 Agent，发送系统消息
            Message sysMsg = new Message();
            sysMsg.setConversationId(convId);
            sysMsg.setSenderId(0L);
            sysMsg.setSenderType(3); // system
            sysMsg.setContent("没有找到被提及的 Agent: " + mentions);
            sysMsg.setMessageType(3);
            messageRepository.insert(sysMsg);
            broadcastMessage(convId, sysMsg);
            return sysMsg;
        }

        // 调用 Orchestrator 处理
        String orchestratorResponse = orchestrator.processGroupChat(convId, content,
                agents.stream().map(com.agenthub.model.entity.Agent::getId).toList());

        Message orchestratorMsg = new Message();
        orchestratorMsg.setConversationId(convId);
        orchestratorMsg.setSenderId(0L);
        orchestratorMsg.setSenderType(3); // orchestrator
        orchestratorMsg.setContent(orchestratorResponse);
        orchestratorMsg.setMessageType(1);
        messageRepository.insert(orchestratorMsg);

        conversationService.updateLastMessageTime(convId);

        // 广播汇总消息
        broadcastMessage(convId, orchestratorMsg);

        return orchestratorMsg;
    }

    /**
     * 通过 WebSocket 广播消息
     */
    private void broadcastMessage(Long convId, Message message) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "message",
                    "conversationId", convId,
                    "message", Map.of(
                            "id", message.getId(),
                            "senderId", message.getSenderId(),
                            "senderType", message.getSenderType(),
                            "content", message.getContent(),
                            "messageType", message.getMessageType(),
                            "createdAt", message.getCreatedAt() != null
                                    ? message.getCreatedAt().toString()
                                    : LocalDateTime.now().toString()
                    )
            );
            webSocketHandler.broadcastToConversation(convId, payload);
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }

    public List<Message> getMessages(Long convId) {
        List<Message> messages = messageRepository.selectList(
            Wrappers.<Message>lambdaQuery()
                .eq(Message::getConversationId, convId)
                .orderByAsc(Message::getCreatedAt)
        );

        for (Message msg : messages) {
            List<MessageBlock> blocks = messageBlockRepository.selectList(
                Wrappers.<MessageBlock>lambdaQuery()
                    .eq(MessageBlock::getMessageId, msg.getId())
            );
            msg.setBlocks(blocks);
        }

        return messages;
    }

    /**
     * 保存富媒体 Artifact
     */
    @Transactional
    public MessageBlock saveArtifact(Long messageId, MessageBlock block) {
        block.setMessageId(messageId);
        messageBlockRepository.insert(block);
        return block;
    }
}
