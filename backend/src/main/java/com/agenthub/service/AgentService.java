package com.agenthub.service;

import com.agenthub.agent.adapter.AgentAdapter;
import com.agenthub.agent.core.AgentRequest;
import com.agenthub.agent.core.AgentResponse;
import com.agenthub.model.entity.Agent;
import com.agenthub.model.entity.Conversation;
import com.agenthub.model.entity.ConversationParticipant;
import com.agenthub.repository.AgentRepository;
import com.agenthub.repository.ConversationParticipantRepository;
import com.agenthub.repository.ConversationRepository;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class AgentService {

    private final List<AgentAdapter> adapters;
    private final AgentRepository agentRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;

    public AgentService(List<AgentAdapter> adapters, AgentRepository agentRepository,
                        ConversationRepository conversationRepository,
                        ConversationParticipantRepository conversationParticipantRepository) {
        this.adapters = adapters;
        this.agentRepository = agentRepository;
        this.conversationRepository = conversationRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
    }

    public List<Agent> getPublicAgents() {
        return agentRepository.selectList(
            Wrappers.<Agent>lambdaQuery()
                .eq(Agent::getIsPublic, true)
        );
    }

    public Agent getAgentById(Long id) {
        return agentRepository.selectById(id);
    }

    public Agent createAgent(Agent agent) {
        agentRepository.insert(agent);
        return agent;
    }

    /**
     * 单聊：调用会话关联的 Agent
     */
    public AgentResponse chatWithAgent(Long convId, String content) {
        log.info("Agent processing message for conversation {}", convId);

        // 1. 查找会话关联的 Agent
        ConversationParticipant participant = conversationParticipantRepository.selectOne(
            Wrappers.<ConversationParticipant>lambdaQuery()
                .eq(ConversationParticipant::getConversationId, convId)
                .isNotNull(ConversationParticipant::getAgentId)
        );

        if (participant == null || participant.getAgentId() == null) {
            log.warn("会话 {} 没有关联的 Agent，使用默认适配器", convId);
            return chatWithDefaultAdapter(content, null);
        }

        // 2. 查找 Agent 信息
        Agent agent = agentRepository.selectById(participant.getAgentId());
        if (agent == null) {
            log.warn("Agent {} 不存在，使用默认适配器", participant.getAgentId());
            return chatWithDefaultAdapter(content, null);
        }

        log.info("调用 Agent: {} (provider: {})", agent.getName(), agent.getProvider());

        // 3. 查找对应的适配器
        AgentAdapter adapter = findAdapter(agent.getProvider(), agent.getId());
        if (adapter == null) {
            log.warn("没有找到 Agent {} 的适配器，使用默认适配器", agent.getName());
            return chatWithDefaultAdapter(content, agent);
        }

        // 4. 调用适配器
        return callAdapter(adapter, agent, content);
    }

    /**
     * 使用默认适配器（system-default）
     */
    public String chat(Long convId, String content) {
        AgentResponse response = chatWithAgent(convId, content);
        return response != null ? response.getContent() : "抱歉，Agent 服务暂时不可用。";
    }

    private AgentAdapter findAdapter(String provider, Long agentId) {
        String agentKey = provider + "-" + agentId;

        // 先尝试精确匹配
        for (AgentAdapter adapter : adapters) {
            if (adapter.supports(agentKey)) {
                return adapter;
            }
        }

        // 再尝试 provider 前缀匹配
        for (AgentAdapter adapter : adapters) {
            if (adapter.supports(provider + "-")) {
                return adapter;
            }
        }

        // 最后尝试 system-default
        for (AgentAdapter adapter : adapters) {
            if (adapter.supports("system-default")) {
                return adapter;
            }
        }

        return adapters.isEmpty() ? null : adapters.get(0);
    }

    private AgentResponse callAdapter(AgentAdapter adapter, Agent agent, String userMessage) {
        try {
            AgentRequest request = new AgentRequest();
            request.setAgentId(agent.getProvider() + "-" + agent.getId());
            request.setSystemPrompt(agent.getSystemPrompt());

            AgentRequest.ChatMessage msg = new AgentRequest.ChatMessage();
            msg.setRole("user");
            msg.setContent(userMessage);
            request.setMessages(Collections.singletonList(msg));

            AgentResponse response = adapter.chat(request);
            return response != null ? response : createEmptyResponse();
        } catch (Exception e) {
            log.error("调用 Agent {} 出错", agent.getName(), e);
            return createErrorResponse("调用 " + agent.getName() + " 出错: " + e.getMessage());
        }
    }

    private AgentResponse chatWithDefaultAdapter(String userMessage, Agent agent) {
        AgentAdapter adapter = findAdapter("system", 0L);
        if (adapter == null) {
            return createErrorResponse("没有可用的 Agent 适配器");
        }

        AgentRequest request = new AgentRequest();
        request.setAgentId("system-default");
        request.setSystemPrompt(agent != null ? agent.getSystemPrompt() : "你是 AgentHub 的 AI 助手。");

        AgentRequest.ChatMessage msg = new AgentRequest.ChatMessage();
        msg.setRole("user");
        msg.setContent(userMessage);
        request.setMessages(Collections.singletonList(msg));

        try {
            AgentResponse response = adapter.chat(request);
            return response != null ? response : createEmptyResponse();
        } catch (Exception e) {
            log.error("调用默认适配器出错", e);
            return createErrorResponse("Agent 服务暂时不可用: " + e.getMessage());
        }
    }

    private AgentResponse createEmptyResponse() {
        AgentResponse resp = new AgentResponse();
        resp.setContent("Agent 暂无响应");
        return resp;
    }

    private AgentResponse createErrorResponse(String message) {
        AgentResponse resp = new AgentResponse();
        resp.setContent(message);
        return resp;
    }
}
