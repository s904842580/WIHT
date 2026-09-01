package com.waht.platform.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Python 审批成功后提交的草稿，字段约束与普通笔记编辑保持一致。
 */
public record AgentNoteDraftRequest(
        @NotBlank(message = "不能为空") @Size(max = 120, message = "长度不能超过 120") String title,
        @Size(max = 500, message = "长度不能超过 500") String summary,
        @NotNull(message = "不能为空") @Size(max = 200000, message = "长度不能超过 200000") String content,
        @NotNull(message = "不能为空") @Positive(message = "必须为正整数") Long categoryId,
        @Valid @Size(max = 10, message = "最多选择 10 个标签") List<
                @NotNull(message = "不能包含空值") @Positive(message = "必须为正整数") Long> tagIds
) {
    public AgentNoteDraftRequest {
        tagIds = tagIds == null ? List.of() : List.copyOf(tagIds);
    }
}
