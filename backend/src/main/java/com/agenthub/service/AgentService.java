package com.agenthub.service;

import com.agenthub.agent.adapter.AgentAdapter;
import com.agenthub.agent.core.AgentRequest;
import com.agenthub.agent.core.AgentResponse;
import com.agenthub.model.entity.Agent;
import com.agenthub.repository.AgentRepository;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AgentService {

    private final List<AgentAdapter> adapters;
    private final AgentRepository agentRepository;

    public AgentService(List<AgentAdapter> adapters, AgentRepository agentRepository) {
        this.adapters = adapters;
        this.agentRepository = agentRepository;
    }

    public List<Agent> getPublicAgents() {
        return agentRepository.selectList(
            Wrappers.<Agent>lambdaQuery()
                .eq(Agent::getIsPublic, true)
        );
    }

    public String chat(Long convId, String content) {
        log.info("Agent processing message for conversation {}: {}", convId, content);

        // MVP: 使用第一个支持 custom 的适配器
        AgentAdapter adapter = adapters.stream()
                .filter(a -> a.supports("system-default"))
                .findFirst()
                .orElse(adapters.get(0));

        AgentRequest request = new AgentRequest();
        request.setAgentId("system-default");

        AgentRequest.ChatMessage msg = new AgentRequest.ChatMessage();
        msg.setRole("user");
        msg.setContent(content);
        request.setMessages(List.of(msg));

        AgentResponse response = adapter.chat(request);
        return response != null ? response.getContent() : "抱歉，Agent 服务暂时不可用。";
    }
}
