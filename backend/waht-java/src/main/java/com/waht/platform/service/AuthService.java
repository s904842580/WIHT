package com.waht.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waht.platform.common.exception.BusinessException;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.common.security.JwtTokenProvider;
import com.waht.platform.common.security.TokenResult;
import com.waht.platform.dto.LoginRequest;
import com.waht.platform.dto.RegisterRequest;
import com.waht.platform.entity.UserEntity;
import com.waht.platform.mapper.UserMapper;
import com.waht.platform.vo.LoginResponse;
import com.waht.platform.vo.UserInfoResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DEFAULT_ROLE = "USER";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        ensureActive(user);

        UserEntity updateUser = new UserEntity();
        updateUser.setId(user.getId());
        updateUser.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(updateUser);

        TokenResult tokenResult = jwtTokenProvider.createToken(user);
        return new LoginResponse(
                "Bearer",
                tokenResult.token(),
                tokenResult.expiresIn(),
                UserInfoResponse.from(user)
        );
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        boolean exists = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)) > 0;
        if (exists) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(resolveNickname(request.getNickname(), username));
        user.setRole(DEFAULT_ROLE);
        user.setStatus(ACTIVE_STATUS);
        userMapper.insert(user);

        TokenResult tokenResult = jwtTokenProvider.createToken(user);
        return new LoginResponse(
                "Bearer",
                tokenResult.token(),
                tokenResult.expiresIn(),
                UserInfoResponse.from(user)
        );
    }

    public UserInfoResponse getCurrentUser(CurrentUser currentUser) {
        UserEntity user = userMapper.selectById(currentUser.userId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录用户不存在");
        }
        ensureActive(user);
        return UserInfoResponse.from(user);
    }

    private void ensureActive(UserEntity user) {
        if (!ACTIVE_STATUS.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户已被禁用");
        }
    }

    private String resolveNickname(String nickname, String username) {
        if (nickname == null || nickname.isBlank()) {
            return username;
        }
        return nickname.trim();
    }
}
