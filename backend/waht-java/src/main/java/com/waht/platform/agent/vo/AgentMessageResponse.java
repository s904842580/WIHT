package com.waht.platform.agent.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习助手对话消息，sources 只对使用过笔记的助手消息有值。
 */
public record AgentMessageResponse(
        String id,
        String role,
        String content,
        List<AgentNoteSourceResponse> sources,
        LocalDateTime createdAt
) {
}
