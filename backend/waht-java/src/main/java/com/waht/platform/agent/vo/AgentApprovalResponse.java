package com.waht.platform.agent.vo;

/**
 * 高风险写操作的审批状态，createdNoteId 仅在写入成功后存在。
 */
public record AgentApprovalResponse(
        String id,
        String status,
        String action,
        AgentDraftResponse draft,
        Long createdNoteId
) {
}
