package com.example.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {
    // 登录用户名，必须唯一
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3 到 20 个字符之间")
    private String username;

    // 明文密码只允许出现在请求体中
    // 保存到数据库前必须用 PasswordEncoder 加密
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在 6 到 50 个字符之间")
    private String password;

    // 用户昵称，用于页面展示
    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称不能超过 30 个字符")
    private String nickname;

    // 用户头像地址
    // 这是可选字段：前端没有传时，后端保存为 null，页面会用昵称首字作为默认头像。
    @Size(max = 255, message = "头像地址不能超过 255 个字符")
    private String avatarUrl;

    // 用户角色
    // 按接口文档，只允许 ADMIN 或 WAREHOUSE_ADMIN
    @NotBlank(message = "角色不能为空")
    private String role;

    // 用户状态
    // 1 启用，0 禁用
    @NotNull(message = "状态不能为空")
    private Integer status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
