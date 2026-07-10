package com.waht.platform.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waht.platform.common.exception.BusinessException;
import com.waht.platform.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTests {

    private final JwtProperties jwtProperties = new JwtProperties();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties, objectMapper);

    @Test
    void parseTokenShouldRejectInvalidSubjectAsUnauthorized() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.put("sub", "not-a-number");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> jwtTokenProvider.parseToken(signedToken(payload)));

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getCode());
    }

    @Test
    void parseTokenShouldRejectMissingUsernameAsUnauthorized() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.remove("username");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> jwtTokenProvider.parseToken(signedToken(payload)));

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getCode());
    }

    private Map<String, Object> validPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "1");
        payload.put("username", "admin");
        payload.put("role", "ADMIN");
        payload.put("iat", Instant.now().getEpochSecond());
        payload.put("exp", Instant.now().plusSeconds(600).getEpochSecond());
        return payload;
    }

    private String signedToken(Map<String, Object> payload) throws Exception {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    private String encodeJson(Map<String, Object> data) throws Exception {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(data));
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(key);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
