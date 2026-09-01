package com.waht.platform.agent.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提供给 Python 工具的作者笔记详情，只能通过短期授权读取。
 */
public record AgentNoteDetailResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String content,
        String status,
        Long categoryId,
        String categoryName,
        List<Long> tagIds,
        List<String> tagNames,
        LocalDateTime updatedAt
) {
}
