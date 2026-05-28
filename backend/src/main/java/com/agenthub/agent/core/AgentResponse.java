package com.agenthub.agent.core;

import lombok.Data;
import java.util.List;

@Data
public class AgentResponse {
    private String content;
    private List<ArtifactBlock> blocks;
    private String reasoning; // 思考过程（可选）

    @Data
    public static class ArtifactBlock {
        private String type; // code/diff/web/file/deploy
        private String content;
        private String language;
        private String title;
        private Object metadata;
    }
}
