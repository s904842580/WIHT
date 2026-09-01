package com.waht.platform.agent.vo;

import java.util.List;

/**
 * 一次非流式 Agent 运行结果。
 */
public record AgentTurnResponse(
        String conversationId,
        String runId,
        String status,
        String answer,
        List<AgentNoteSourceResponse> sources,
        AgentApprovalResponse approval
) {
}
