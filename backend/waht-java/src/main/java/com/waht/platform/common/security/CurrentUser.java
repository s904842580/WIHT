package com.waht.platform.common.security;

public record CurrentUser(
        Long userId,
        String username,
        String role
) {
}
