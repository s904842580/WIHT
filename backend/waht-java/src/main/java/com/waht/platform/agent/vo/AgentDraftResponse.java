package com.waht.platform.agent.vo;

import java.util.List;

/**
 * Agent 生成、等待用户确认的结构化笔记草稿。
 */
public record AgentDraftResponse(
        String title,
        String summary,
        String content,
        Long categoryId,
        List<Long> tagIds
) {
}
