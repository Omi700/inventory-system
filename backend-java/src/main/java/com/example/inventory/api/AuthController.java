package com.example.inventory.api;

import com.example.inventory.api.dto.ApiResponse;
import com.example.inventory.api.dto.LoginRequest;
import com.example.inventory.api.dto.LoginResponse;
import com.example.inventory.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("api/auth/login")
    public ApiResponse<LoginResponse> Login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.fail(40001, e.getMessage());
    }
}
