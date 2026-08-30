package com.waht.platform.common.security;

import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.entity.UserEntity;
import com.waht.platform.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 受保护 API 的 JWT 入口校验器，校验成功后把当前用户写入请求属性。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public JwtAuthInterceptor(JwtTokenProvider jwtTokenProvider, UserMapper userMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        CurrentUser tokenUser = jwtTokenProvider.parseToken(token);
        UserEntity user = userMapper.selectById(tokenUser.userId());
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "登录用户不存在或已被禁用");
        }
        CurrentUser currentUser = new CurrentUser(user.getId(), user.getUsername(), user.getRole());
        request.setAttribute(AuthConstants.CURRENT_USER_ATTRIBUTE, currentUser);
        return true;
    }
}
