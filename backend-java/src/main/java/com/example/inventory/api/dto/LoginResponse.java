package com.example.inventory.api.dto;

public class LoginResponse {
    private String token;
    private UserInfo user;

    public LoginResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserInfo getUser() {
        return user;
    }
    // 返回给前端的用户信息，不包含 passwordHash
    public static class UserInfo {

        private Long id;
        private String username;
        private String nickname;
        private String avatarUrl;
        private String role;

        public UserInfo(Long id, String username, String nickname, String avatarUrl, String role) {
            this.id = id;
            this.username = username;
            this.nickname = nickname;
            this.avatarUrl = avatarUrl;
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

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public String getRole() {
            return role;
        }
    }
}
