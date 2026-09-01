package com.waht.platform.agent.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Java 与 Python Agent 服务之间的内部通信配置。
 */
@Component
@Validated
@ConfigurationProperties(prefix = "waht.agent")
public class AgentProperties {

    @NotBlank
    private String baseUrl = "http://127.0.0.1:8000";

    @NotBlank
    private String serviceToken = "waht-local-agent-service-token-change-me";

    @Min(100)
    @Max(30000)
    private int connectTimeoutMillis = 3000;

    @Min(1000)
    @Max(120000)
    private int readTimeoutMillis = 60000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }
}
