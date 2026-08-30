package com.waht.platform.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记控制器中需要注入当前登录用户的参数。
 *
 * <p>使用该注解后，控制器无需直接读取 HttpServletRequest，认证实现可以独立演进。</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}
