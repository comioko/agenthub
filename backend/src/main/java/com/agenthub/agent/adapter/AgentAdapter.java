package com.agenthub.agent.adapter;

import com.agenthub.agent.core.AgentRequest;
import com.agenthub.agent.core.AgentResponse;

/**
 * Agent 适配器接口
 * 所有外部 Agent Provider 必须实现此接口
 */
public interface AgentAdapter {

    /**
     * 获取 Provider 名称
     */
    String getProvider();

    /**
     * 是否支持该 Agent
     */
    boolean supports(String agentId);

    /**
     * 同步对话
     */
    AgentResponse chat(AgentRequest request);

    /**
     * 获取支持的模型列表
     */
    default java.util.List<String> getSupportedModels() {
        return java.util.Collections.emptyList();
    }
}
