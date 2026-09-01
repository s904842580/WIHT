package com.waht.platform.common.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 可外部覆盖配置；生产环境应通过环境变量提供独立密钥。
 */
@Component
@Validated
@ConfigurationProperties(prefix = "waht.security.jwt")
public class JwtProperties {

    @NotBlank
    @Size(min = 32)
    private String secret = "waht-local-dev-secret-change-me-20260629";

    @Min(1)
    private long expirationMinutes = 120;

    @Min(1)
    private long delegationExpirationMinutes = 2;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public long getDelegationExpirationMinutes() {
        return delegationExpirationMinutes;
    }

    public void setDelegationExpirationMinutes(long delegationExpirationMinutes) {
        this.delegationExpirationMinutes = delegationExpirationMinutes;
    }
}
