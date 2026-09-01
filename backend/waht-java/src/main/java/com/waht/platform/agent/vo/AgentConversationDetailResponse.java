package com.waht.platform.agent.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 单个会话的完整消息和当前待审批草稿。
 */
public record AgentConversationDetailResponse(
        String id,
        String title,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AgentMessageResponse> messages,
        AgentApprovalResponse pendingApproval
) {
}
