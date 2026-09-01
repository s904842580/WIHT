package com.waht.platform.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 用户审批时可修改的笔记草稿；最终仍由 NoteService 校验并保存为 DRAFT。
 */
public record AgentDraftRequest(
        @NotBlank(message = "不能为空")
        @Size(max = 120, message = "长度不能超过 120") String title,
        @Size(max = 500, message = "长度不能超过 500") String summary,
        @NotNull(message = "不能为空")
        @Size(max = 200000, message = "长度不能超过 200000") String content,
        @NotNull(message = "不能为空") @Positive(message = "必须为正整数") Long categoryId,
        @Valid @Size(max = 10, message = "最多选择 10 个标签") List<
                @NotNull(message = "不能包含空值") @Positive(message = "必须为正整数") Long> tagIds
) {
    public AgentDraftRequest {
        tagIds = tagIds == null ? List.of() : List.copyOf(tagIds);
    }
}
