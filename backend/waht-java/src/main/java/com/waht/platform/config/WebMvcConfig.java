package com.waht.platform.config;

import com.waht.platform.agent.security.AgentDelegationArgumentResolver;
import com.waht.platform.audit.AuditInterceptor;
import com.waht.platform.agent.security.AgentDelegationInterceptor;
import com.waht.platform.common.security.CurrentUserArgumentResolver;
import com.waht.platform.common.security.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

/**
 * Web 层公共配置：注册跨域规则、登录拦截器和当前用户参数解析器。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final AgentDelegationArgumentResolver agentDelegationArgumentResolver;
    private final AgentDelegationInterceptor agentDelegationInterceptor;
    private final CorsProperties corsProperties;
    private final AuditInterceptor auditInterceptor;

    public WebMvcConfig(
            JwtAuthInterceptor jwtAuthInterceptor,
            CurrentUserArgumentResolver currentUserArgumentResolver,
            AgentDelegationArgumentResolver agentDelegationArgumentResolver,
            AgentDelegationInterceptor agentDelegationInterceptor,
            CorsProperties corsProperties,
            AuditInterceptor auditInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.agentDelegationArgumentResolver = agentDelegationArgumentResolver;
        this.agentDelegationInterceptor = agentDelegationInterceptor;
        this.corsProperties = corsProperties;
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name())
                .allowedHeaders(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "X-Request-Id")
                .exposedHeaders("X-Request-Id")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
        resolvers.add(agentDelegationArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Run before authentication so denied attempts are recorded too.
        registry.addInterceptor(auditInterceptor).addPathPatterns("/api/**").order(-100);
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/notes",
                        "/api/notes/**",
                        "/api/note-categories",
                        "/api/note-tags",
                        "/api/projects",
                        "/api/projects/**",
                        "/api/tech-stacks",
                        "/api/internal/agent-tools/**"
                );
        registry.addInterceptor(agentDelegationInterceptor)
                .addPathPatterns("/api/internal/agent-tools/**");
    }
}
