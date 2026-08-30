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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 使用 HMAC-SHA256 创建和验证 WAHT 登录令牌。
 */
@Component
public class JwtTokenProvider {

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
        payload.put("iat", issuedAt);
        payload.put("exp", expiresAt);

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return new TokenResult(unsignedToken + "." + sign(unsignedToken), expiresIn);
    }

    public CurrentUser parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "登录令牌不能为空");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "登录令牌格式错误");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "登录令牌签名无效");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        long expiresAt = readLongClaim(payload, "exp");
        if (expiresAt <= Instant.now().getEpochSecond()) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "登录令牌已过期");
        }

        Long userId = readLongClaim(payload, "sub");
        String username = readStringClaim(payload, "username");
        String role = readStringClaim(payload, "role");
        return new CurrentUser(userId, username, role);
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
