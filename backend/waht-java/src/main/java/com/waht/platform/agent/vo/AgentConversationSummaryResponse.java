package com.waht.platform.agent.vo;

import java.time.LocalDateTime;

/**
 * 学习助手会话列表项。
 */
public record AgentConversationSummaryResponse(
        String id,
        String title,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
