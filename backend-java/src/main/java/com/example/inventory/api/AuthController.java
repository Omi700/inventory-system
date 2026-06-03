package com.example.inventory.api;

import com.example.inventory.api.dto.ApiResponse;
import com.example.inventory.api.dto.LoginRequest;
import com.example.inventory.api.dto.LoginResponse;
import com.example.inventory.security.CurrentUser;
import com.example.inventory.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.fail(40001, e.getMessage());
    }
    // GET /api/auth/me
    // 用来获取当前登录用户的信息
    @GetMapping("/api/auth/me")
    public ApiResponse<LoginResponse.UserInfo> me(Authentication authentication) {
        // authentication 是 Spring Security 自动传进来的
        // 如果 JWT 过滤器解析成功，这里就能拿到当前用户

        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();

        LoginResponse.UserInfo userInfo = authService.getCurrentUser(currentUser);

        return ApiResponse.success(userInfo);
    }
}
