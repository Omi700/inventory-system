package com.example.inventory.service;

import com.example.inventory.api.dto.CreateUserRequest;
import com.example.inventory.api.dto.LoginResponse;
import com.example.inventory.domain.AppUser;
import com.example.inventory.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }
    // 管理员新增用户
    public LoginResponse.UserInfo createUser(CreateUserRequest request) {
        // 1. 校验用户名唯一
        if (appUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 2. 校验角色是否合法
        if (!"ADMIN".equals(request.getRole()) && !"WAREHOUSE_ADMIN".equals(request.getRole())) {
            throw new IllegalArgumentException("角色只能是 ADMIN 或 WAREHOUSE_ADMIN");
        }

        // 3. 校验状态是否合法
        if (!Integer.valueOf(0).equals(request.getStatus()) && !Integer.valueOf(1).equals(request.getStatus())) {
            throw new IllegalArgumentException("状态只能是 0 或 1");
        }
        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash((passwordEncoder.encode(request.getPassword())));
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        AppUser savedUser = appUserRepository.save(user);
        return new LoginResponse.UserInfo(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getNickname(),
                savedUser.getAvatarUrl(),
                savedUser.getRole()
        );
    }
}
