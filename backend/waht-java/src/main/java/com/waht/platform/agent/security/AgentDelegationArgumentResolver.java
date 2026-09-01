package com.waht.platform.agent.security;

import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.common.security.AgentDelegation;
import com.waht.platform.common.security.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 从请求属性读取内部 Agent 身份，防止控制器自行解析不可信请求字段。
 */
@Component
public class AgentDelegationArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AgentUser.class)
                && AgentDelegation.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Object value = request == null ? null : request.getAttribute(AuthConstants.AGENT_DELEGATION_ATTRIBUTE);
        if (value instanceof AgentDelegation delegation) {
            return delegation;
        }
        throw new ServiceException(ErrorCode.UNAUTHORIZED, "Agent 授权不存在");
    }
}
