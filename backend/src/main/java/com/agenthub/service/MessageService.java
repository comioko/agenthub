package com.agenthub.service;

import com.agenthub.model.entity.Conversation;
import com.agenthub.model.entity.Message;
import com.agenthub.model.entity.MessageBlock;
import com.agenthub.repository.ConversationRepository;
import com.agenthub.repository.MessageBlockRepository;
import com.agenthub.repository.MessageRepository;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageBlockRepository messageBlockRepository;
    private final ConversationRepository conversationRepository;
    private final AgentService agentService;
    private final ConversationService conversationService;

    public Message sendMessage(Long userId, Long convId, String content) {
        Conversation conv = conversationRepository.selectById(convId);
        if (conv == null) {
            throw new RuntimeException("会话不存在");
        }

        Message message = new Message();
        message.setConversationId(convId);
        message.setSenderId(userId);
        message.setSenderType(1); // user
        message.setContent(content);
        message.setMessageType(1); // text
        messageRepository.insert(message);

        conversationService.updateLastMessageTime(convId);

        // Call agent to get response
        String agentResponse = agentService.chat(convId, content);

        Message agentMsg = new Message();
        agentMsg.setConversationId(convId);
        agentMsg.setSenderId(2L); // agent
        agentMsg.setSenderType(2); // agent
        agentMsg.setContent(agentResponse);
        agentMsg.setMessageType(1);
        messageRepository.insert(agentMsg);

        conversationService.updateLastMessageTime(convId);

        return message;
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
}
