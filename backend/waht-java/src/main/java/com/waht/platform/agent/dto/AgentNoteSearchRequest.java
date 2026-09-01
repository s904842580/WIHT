package com.waht.platform.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Agent 的受控笔记搜索条件，限制返回数量以控制上下文和数据库压力。
 */
public record AgentNoteSearchRequest(
        @NotBlank(message = "不能为空") @Size(max = 100, message = "长度不能超过 100") String keyword,
        @Positive(message = "必须为正整数") Long categoryId,
        @Valid @Size(max = 10, message = "最多选择 10 个标签") List<
                @NotNull(message = "不能包含空值") @Positive(message = "必须为正整数") Long> tagIds,
        @Min(value = 1, message = "不能小于 1") @Max(value = 10, message = "不能大于 10") int limit
) {
    public AgentNoteSearchRequest {
        tagIds = tagIds == null ? List.of() : List.copyOf(tagIds);
        limit = limit == 0 ? 5 : limit;
    }
}
