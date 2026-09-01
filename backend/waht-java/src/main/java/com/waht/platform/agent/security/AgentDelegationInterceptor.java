package com.waht.platform.agent.security;

import com.waht.platform.agent.config.AgentProperties;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.common.security.AgentDelegation;
import com.waht.platform.common.security.AuthConstants;
import com.waht.platform.common.security.JwtTokenProvider;
import com.waht.platform.entity.UserEntity;
import com.waht.platform.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 内部笔记工具的双重认证：共享服务令牌确认调用方，delegation JWT 确认用户和权限。
 */
@Component
public class AgentDelegationInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SERVICE_TOKEN_HEADER = "X-WAHT-Service-Token";
    private static final String NOTE_READ_SCOPE = "note:read";
    private static final String NOTE_CREATE_DRAFT_SCOPE = "note:create-draft";

    private final AgentProperties agentProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public AgentDelegationInterceptor(
            AgentProperties agentProperties,
            JwtTokenProvider jwtTokenProvider,
            UserMapper userMapper) {
        this.agentProperties = agentProperties;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        verifyServiceToken(request.getHeader(SERVICE_TOKEN_HEADER));

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "缺少 Agent 授权令牌");
        }
        String scope = request.getRequestURI().endsWith("/note-drafts")
                ? NOTE_CREATE_DRAFT_SCOPE
                : NOTE_READ_SCOPE;
        AgentDelegation tokenDelegation = jwtTokenProvider.parseAgentDelegationToken(
                authorization.substring(BEARER_PREFIX.length()).trim(),
                scope
        );
        UserEntity user = userMapper.selectById(tokenDelegation.userId());
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "Agent 授权用户不存在或已被禁用");
        }
        AgentDelegation delegation = new AgentDelegation(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                tokenDelegation.runId(),
                tokenDelegation.scopes()
        );
        request.setAttribute(AuthConstants.AGENT_DELEGATION_ATTRIBUTE, delegation);
        return true;
    }

    private void verifyServiceToken(String actualToken) {
        if (!StringUtils.hasText(actualToken)
                || !MessageDigest.isEqual(
                agentProperties.getServiceToken().getBytes(StandardCharsets.UTF_8),
                actualToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "Agent 服务认证失败");
        }
    }
}
