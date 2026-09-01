package com.waht.platform.agent.client;

import com.waht.platform.agent.dto.AgentDraftRequest;

/**
 * Java 调用 Python 时使用的内部请求模型，不暴露给浏览器。
 */
final class AgentServicePayloads {

    private AgentServicePayloads() {
    }

    record Actor(Long userId, String username) {
    }

    record CreateConversation(Actor actor, String title) {
    }

    record Run(Actor actor, String runId, String message, String delegationToken) {
    }

    record Approval(Actor actor, String decision, String delegationToken, AgentDraftRequest draft) {
    }
}
