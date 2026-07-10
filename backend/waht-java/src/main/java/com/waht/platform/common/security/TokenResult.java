package com.waht.platform.common.security;

public record TokenResult(
        String token,
        long expiresIn
) {
}
