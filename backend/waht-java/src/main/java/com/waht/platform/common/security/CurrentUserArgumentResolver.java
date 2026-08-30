package com.waht.platform.common.security;

import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 把 JWT 拦截器保存的请求属性转换为控制器可直接使用的 CurrentUser。
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class)
                && CurrentUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Object user = request == null ? null : request.getAttribute(AuthConstants.CURRENT_USER_ATTRIBUTE);
        if (user instanceof CurrentUser currentUser) {
            return currentUser;
        }
        throw new ServiceException(ErrorCode.UNAUTHORIZED, "请先登录");
    }
}
