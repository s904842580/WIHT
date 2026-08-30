package com.waht.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 跨域白名单配置。
 *
 * <p>本地默认允许 Vite 的两个常用地址，部署时可通过
 * {@code WAHT_CORS_ALLOWED_ORIGINS} 使用逗号分隔的地址覆盖。</p>
 */
@Component
@ConfigurationProperties(prefix = "waht.web.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    ));

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null ? new ArrayList<>() : new ArrayList<>(allowedOrigins);
    }
}
