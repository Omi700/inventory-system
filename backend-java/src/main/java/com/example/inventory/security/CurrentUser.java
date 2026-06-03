package com.example.inventory.security;

// 这个类表示“当前已经登录的用户”
// 它会被放进 Spring Security 的上下文里
public class CurrentUser {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String role;

    public CurrentUser(Long id, String username, String nickname, String role) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRole() {
        return role;
    }
}