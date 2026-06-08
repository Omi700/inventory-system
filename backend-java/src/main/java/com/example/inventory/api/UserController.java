package com.example.inventory.api;

import com.example.inventory.api.dto.ApiResponse;
import com.example.inventory.api.dto.CreateUserRequest;
import com.example.inventory.api.dto.LoginResponse;
import com.example.inventory.security.CurrentUser;
import com.example.inventory.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users
    // 管理员新增用户
    @PostMapping("/api/users")
    public ApiResponse<LoginResponse.UserInfo> createUser(
            Authentication authentication,
            @Valid @RequestBody CreateUserRequest request
    ) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();

        // 接口文档要求：只有 ADMIN 可以新增用户
        if (!"ADMIN".equals(currentUser.getRole())) {
            return ApiResponse.fail(40300, "权限不足");
        }

        LoginResponse.UserInfo user = userService.createUser(request);
        return ApiResponse.success(user);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.fail(40001, e.getMessage());
    }
}