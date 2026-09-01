package com.waht.platform.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 向学习助手发送一条非流式消息。
 */
public record SendAgentMessageRequest(
        @NotBlank(message = "不能为空")
        @Size(max = 8000, message = "长度不能超过 8000") String message
) {
}
