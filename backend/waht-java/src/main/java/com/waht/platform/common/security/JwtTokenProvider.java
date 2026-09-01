package com.waht.platform.common.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 使用 HMAC-SHA256 创建和验证 WAHT 登录令牌。
 */
@Component
public class JwtTokenProvider {

    private static final String TOKEN_USE_USER = "user";
    private static final String TOKEN_USE_AGENT_DELEGATION = "agent_delegation";
    private static final String AGENT_AUDIENCE = "waht-agent";

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(JwtProperties jwtProperties, ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.objectMapper = objectMapper;
    }

    public TokenResult createToken(UserEntity user) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresIn = jwtProperties.getExpirationMinutes() * 60;
        long expiresAt = issuedAt + expiresIn;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(user.getId()));
        payload.put("username", user.getUsername());
        payload.put("role", user.getRole());
        payload.put("token_use", TOKEN_USE_USER);
        payload.put("iat", issuedAt);
        payload.put("exp", expiresAt);

        return new TokenResult(createSignedToken(header, payload), expiresIn);
    }

    /**
     * 给 Python Agent 签发一次运行专用的最小权限令牌，默认两分钟失效。
     */
    public String createAgentDelegationToken(CurrentUser user, String runId, Set<String> scopes) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + jwtProperties.getDelegationExpirationMinutes() * 60;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(user.userId()));
        payload.put("username", user.username());
        payload.put("role", user.role());
        payload.put("token_use", TOKEN_USE_AGENT_DELEGATION);
        payload.put("aud", AGENT_AUDIENCE);
        payload.put("run_id", runId);
        payload.put("scopes", scopes);
        payload.put("iat", issuedAt);
        payload.put("exp", expiresAt);
        return createSignedToken(header, payload);
    }

    public CurrentUser parseToken(String token) {
        Map<String, Object> payload = decodeVerifiedPayload(token);
        String tokenUse = readOptionalStringClaim(payload, "token_use");
        if (tokenUse != null && !TOKEN_USE_USER.equals(tokenUse)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "登录令牌用途无效");
        }
        Long userId = readLongClaim(payload, "sub");
        String username = readStringClaim(payload, "username");
        String role = readStringClaim(payload, "role");
        return new CurrentUser(userId, username, role);
    }

    public AgentDelegation parseAgentDelegationToken(String token, String requiredScope) {
        Map<String, Object> payload = decodeVerifiedPayload(token);
        if (!TOKEN_USE_AGENT_DELEGATION.equals(readStringClaim(payload, "token_use"))
                || !AGENT_AUDIENCE.equals(readStringClaim(payload, "aud"))) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "Agent 授权令牌用途无效");
        }
        Set<String> scopes = readScopes(payload);
        if (!scopes.contains(requiredScope)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "Agent 授权范围不足");
        }
        return new AgentDelegation(
                readLongClaim(payload, "sub"),
                readStringClaim(payload, "username"),
                readStringClaim(payload, "role"),
                readStringClaim(payload, "run_id"),
                scopes
        );
    }

    private Map<String, Object> decodeVerifiedPayload(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "令牌不能为空");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "令牌格式错误");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "令牌签名无效");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        long expiresAt = readLongClaim(payload, "exp");
        if (expiresAt <= Instant.now().getEpochSecond()) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "令牌已过期");
        }
        return payload;
    }

    private String createSignedToken(Map<String, Object> header, Map<String, Object> payload) {
        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    private String encodeJson(Map<String, Object> data) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(data);
            return BASE64_URL_ENCODER.encodeToString(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to encode jwt json", ex);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            byte[] json = BASE64_URL_DECODER.decode(value);
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (IllegalArgumentException | IOException ex) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "登录令牌内容无效");
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(
                    jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(key);
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign jwt", ex);
        }
    }

    private long readLongClaim(Map<String, Object> payload, String claimName) {
        Object value = payload.get(claimName);
        try {
            return readLong(value);
        } catch (NumberFormatException ex) {
            throw invalidTokenContent();
        }
    }

    private String readStringClaim(Map<String, Object> payload, String claimName) {
        Object value = payload.get(claimName);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            throw invalidTokenContent();
        }
        return String.valueOf(value);
    }

    private String readOptionalStringClaim(Map<String, Object> payload, String claimName) {
        Object value = payload.get(claimName);
        return value == null ? null : String.valueOf(value);
    }

    private Set<String> readScopes(Map<String, Object> payload) {
        Object value = payload.get("scopes");
        if (!(value instanceof Collection<?> collection)) {
            throw invalidTokenContent();
        }
        Set<String> scopes = new LinkedHashSet<>();
        for (Object item : collection) {
            if (item == null || !StringUtils.hasText(String.valueOf(item))) {
                throw invalidTokenContent();
            }
            scopes.add(String.valueOf(item));
        }
        return Set.copyOf(scopes);
    }

    private long readLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private ServiceException invalidTokenContent() {
        return new ServiceException(ErrorCode.UNAUTHORIZED, "登录令牌内容无效");
    }
}
