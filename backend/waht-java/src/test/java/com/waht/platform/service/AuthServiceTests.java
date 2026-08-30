package com.waht.platform.service;

import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.common.security.JwtTokenProvider;
import com.waht.platform.common.security.TokenResult;
import com.waht.platform.dto.LoginRequest;
import com.waht.platform.dto.RegisterRequest;
import com.waht.platform.entity.UserEntity;
import com.waht.platform.mapper.UserMapper;
import com.waht.platform.vo.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/**
 * AuthService 的核心成功与失败分支测试，不依赖真实数据库。
 */
class AuthServiceTests {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService = new AuthService(userMapper, passwordEncoder, jwtTokenProvider);

    @Test
    void registerShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new_user");
        request.setPassword("123456");
        request.setNickname("new user");

        when(userMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(10L);
            return 1;
        }).when(userMapper).insert(any(UserEntity.class));
        when(jwtTokenProvider.createToken(any(UserEntity.class))).thenReturn(new TokenResult("token-value", 7200));

        LoginResponse response = authService.register(request);

        assertEquals("token-value", response.getToken());
        assertEquals("new_user", response.getUser().getUsername());
        assertEquals("USER", response.getUser().getRole());
        verify(userMapper).insert(any(UserEntity.class));
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        when(userMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class, () -> authService.register(request));

        assertEquals(ErrorCode.CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void loginShouldRejectWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setStatus("ACTIVE");

        when(userMapper.selectOne(any())).thenReturn(user);

        ServiceException exception = assertThrows(ServiceException.class, () -> authService.login(request));

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getCode());
    }
}
