package com.waht.platform.controller;

import com.waht.platform.common.api.ApiResponse;
import com.waht.platform.common.security.AuthConstants;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.dto.LoginRequest;
import com.waht.platform.dto.RegisterRequest;
import com.waht.platform.service.AuthService;
import com.waht.platform.vo.LoginResponse;
import com.waht.platform.vo.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me(
            @RequestAttribute(AuthConstants.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        return ApiResponse.success(authService.getCurrentUser(currentUser));
    }
}
