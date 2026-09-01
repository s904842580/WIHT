package com.waht.platform.agent.vo;

import com.waht.platform.vo.NoteCategoryResponse;
import com.waht.platform.vo.NoteTagResponse;

import java.util.List;

/**
 * Agent 生成草稿时可使用的有效分类和标签。
 */
public record AgentNoteMetadataResponse(
        List<NoteCategoryResponse> categories,
        List<NoteTagResponse> tags
) {
}
