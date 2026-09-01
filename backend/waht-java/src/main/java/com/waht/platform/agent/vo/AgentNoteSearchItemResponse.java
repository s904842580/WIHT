package com.waht.platform.agent.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提供给 Python 工具的轻量笔记搜索结果，不返回完整正文。
 */
public record AgentNoteSearchItemResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String status,
        String categoryName,
        List<String> tagNames,
        LocalDateTime updatedAt
) {
}
