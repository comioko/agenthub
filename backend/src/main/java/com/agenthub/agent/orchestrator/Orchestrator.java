package com.agenthub.agent.orchestrator;

import com.agenthub.agent.adapter.AgentAdapter;
import com.agenthub.agent.core.AgentRequest;
import com.agenthub.agent.core.AgentResponse;
import com.agenthub.model.entity.Agent;
import com.agenthub.repository.AgentRepository;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class Orchestrator {

    private final List<AgentAdapter> adapters;
    private final AgentRepository agentRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * 处理群聊消息，解析 @ 提及并分派任务
     */
    public String processGroupChat(Long conversationId, String content, List<Long> mentionedAgentIds) {
        if (mentionedAgentIds == null || mentionedAgentIds.isEmpty()) {
            return null;
        }

        // 解析 @ 提及的 Agent
        List<Agent> agents = agentRepository.selectList(
                Wrappers.<Agent>lambdaQuery()
                        .in(Agent::getId, mentionedAgentIds)
        );

        if (agents.isEmpty()) {
            return "没有找到被提及的 Agent";
        }

        // 如果只提及了一个 Agent，直接调用
        if (agents.size() == 1) {
            return callAgent(agents.get(0), content);
        }

        // 多个 Agent，并行调用并汇总结果
        return processMultipleAgents(agents, content);
    }

    /**
     * 解析消息中的 @ 提及
     * 格式: @AgentName 或 @AgentName1 @AgentName2
     */
    public List<String> parseMentions(String content) {
        List<String> mentions = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return mentions;
        }

        // 匹配 @ 后面跟的非空白字符
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@(\\S+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions;
    }

    /**
     * 根据 Agent 名称查找 Agent
     */
    public List<Agent> findAgentsByMentions(List<String> agentNames) {
        if (agentNames == null || agentNames.isEmpty()) {
            return Collections.emptyList();
        }

        return agentRepository.selectList(
                Wrappers.<Agent>lambdaQuery()
                        .in(Agent::getName, agentNames)
                        .eq(Agent::getIsPublic, true)
        );
    }

    /**
     * 并行调用多个 Agent 并汇总结果
     */
    private String processMultipleAgents(List<Agent> agents, String userMessage) {
        log.info("群聊任务分派给 {} 个 Agent", agents.size());

        // 并行执行所有 Agent 任务
        List<CompletableFuture<AgentResult>> futures = agents.stream()
                .map(agent -> CompletableFuture.supplyAsync(() -> {
                    String result = callAgent(agent, userMessage);
                    return new AgentResult(agent.getName(), result);
                }, executor))
                .collect(Collectors.toList());

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try {
            allFutures.join();
        } catch (Exception e) {
            log.error("并行执行 Agent 任务出错", e);
        }

        // 收集结果
        StringBuilder summary = new StringBuilder();
        summary.append("【协作完成】\n\n");
        for (CompletableFuture<AgentResult> future : futures) {
            AgentResult result = future.join();
            summary.append("🤖 **").append(result.agentName).append("**:\n");
            summary.append(result.response).append("\n\n");
        }

        return summary.toString();
    }

    /**
     * 调用单个 Agent
     */
    private String callAgent(Agent agent, String message) {
        log.info("调用 Agent: {} (provider: {})", agent.getName(), agent.getProvider());

        // 找到对应的适配器
        AgentAdapter adapter = adapters.stream()
                .filter(a -> a.supports(agent.getProvider() + "-" + agent.getId()))
                .findFirst()
                .orElseGet(() -> adapters.stream()
                        .filter(a -> a.supports("system-default"))
                        .findFirst()
                        .orElse(null));

        if (adapter == null) {
            return "没有可用的 Agent 适配器";
        }

        try {
            AgentRequest request = new AgentRequest();
            request.setAgentId(agent.getProvider() + "-" + agent.getId());
            request.setSystemPrompt(agent.getSystemPrompt());

            AgentRequest.ChatMessage msg = new AgentRequest.ChatMessage();
            msg.setRole("user");
            msg.setContent(message);
            request.setMessages(Collections.singletonList(msg));

            AgentResponse response = adapter.chat(request);
            return response != null && response.getContent() != null
                    ? response.getContent()
                    : "Agent 暂无响应";
        } catch (Exception e) {
            log.error("调用 Agent {} 出错", agent.getName(), e);
            return "调用 " + agent.getName() + " 出错: " + e.getMessage();
        }
    }

    private static class AgentResult {
        String agentName;
        String response;

        AgentResult(String agentName, String response) {
            this.agentName = agentName;
            this.response = response;
        }
    }
}
