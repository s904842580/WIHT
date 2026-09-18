package com.waht.platform.controller;

import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.audit.Audited;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.common.security.LoginUser;
import com.waht.platform.dto.LoginRequest;
import com.waht.platform.dto.RegisterRequest;
import com.waht.platform.service.AuthService;
import com.waht.platform.vo.LoginResponse;
import com.waht.platform.vo.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户注册、登录和当前登录用户查询接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Audited(module = "AUTH", action = "LOGIN")
    public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return BaseResponse.success(authService.login(request));
    }

    @PostMapping("/register")
    @Audited(module = "AUTH", action = "REGISTER")
    public BaseResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return BaseResponse.success(authService.register(request));
    }

    @GetMapping("/me")
    public BaseResponse<UserInfoResponse> me(
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(authService.getCurrentUser(currentUser));
    }
}
