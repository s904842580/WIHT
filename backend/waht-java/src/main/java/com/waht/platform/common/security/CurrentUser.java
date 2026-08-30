package com.waht.platform.common.security;

/**
 * 单次请求内可信的登录用户快照，由认证拦截器从数据库用户记录构造。
 */
public record CurrentUser(
        Long userId,
        String username,
        String role
) {
}
