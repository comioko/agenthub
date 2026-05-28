package com.agenthub.service;

import com.agenthub.model.entity.Conversation;
import com.agenthub.model.entity.ConversationParticipant;
import com.agenthub.repository.ConversationParticipantRepository;
import com.agenthub.repository.ConversationRepository;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;

    public Conversation createConversation(Long userId, String name, Integer type) {
        Conversation conv = new Conversation();
        conv.setName(name);
        conv.setType(type);
        conv.setOwnerId(userId);
        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepository.insert(conv);

        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversationId(conv.getId());
        participant.setUserId(userId);
        participant.setRole(1);
        participantRepository.insert(participant);

        return conv;
    }

    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.selectList(
            Wrappers.<Conversation>lambdaQuery()
                .eq(Conversation::getOwnerId, userId)
                .orderByDesc(Conversation::getLastMessageAt)
        );
    }

    public Conversation getConversation(Long convId) {
        return conversationRepository.selectById(convId);
    }

    public void updateLastMessageTime(Long convId) {
        conversationRepository.update(
            null,
            Wrappers.<Conversation>lambdaUpdate()
                .eq(Conversation::getId, convId)
                .set(Conversation::getLastMessageAt, LocalDateTime.now())
        );
    }
}
