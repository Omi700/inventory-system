package com.example.inventory.service;

import com.example.inventory.api.dto.LoginRequest;
import com.example.inventory.api.dto.LoginResponse;
import com.example.inventory.domain.AppUser;
import com.example.inventory.repository.AppUserRepository;
import com.example.inventory.security.CurrentUser;
import com.example.inventory.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    // 登陆主流程：查用户 -> 校验状态 -> 校验密码 -> 生成 token
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.getUsername()).
                orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new IllegalArgumentException("用户已禁用");
        }
        boolean passwordMatched = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!passwordMatched) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtService.generateToken(user); //生成token
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole()
        );
        return new LoginResponse(token, userInfo);
    }
    // 根据当前登录用户对象，返回前端需要的用户信息
    public LoginResponse.UserInfo getCurrentUser(CurrentUser currentUser) {
        return new LoginResponse.UserInfo(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getNickname(),
                currentUser.getRole()
        );
    }
}
