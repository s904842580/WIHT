package com.waht.platform.common.security;

import java.util.Set;

/**
 * Java 为单次 Agent 运行签发的短期授权，不等同于浏览器登录令牌。
 */
public record AgentDelegation(
        Long userId,
        String username,
        String role,
        String runId,
        Set<String> scopes
) {
}
