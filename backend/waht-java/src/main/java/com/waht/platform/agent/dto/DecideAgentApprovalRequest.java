package com.waht.platform.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 处理待审批草稿；EDIT 时 draft 必填，Service 会做条件校验。
 */
public record DecideAgentApprovalRequest(
        @NotNull(message = "不能为空") AgentApprovalDecision decision,
        @Valid AgentDraftRequest draft
) {
}
