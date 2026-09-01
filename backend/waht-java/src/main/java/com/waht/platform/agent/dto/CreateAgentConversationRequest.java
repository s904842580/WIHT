package com.waht.platform.agent.dto;

import jakarta.validation.constraints.Size;

/**
 * 创建学习助手对话，可选标题用于区分不同学习主题。
 */
public record CreateAgentConversationRequest(
        @Size(max = 120, message = "长度不能超过 120") String title
) {
}
