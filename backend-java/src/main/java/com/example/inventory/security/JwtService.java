package com.example.inventory.security;

import com.example.inventory.api.dto.LoginResponse;
import com.example.inventory.domain.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private  final SecretKey secretKey;
    private  final long expireHours;

    public JwtService(@Value("${inventory.jwt.secret}") String secret,
                      @Value("${inventory.jwt.expire-hours}") long expireHours) {
        this.secretKey = Keys.hmacShaKeyFor((secret).getBytes(StandardCharsets.UTF_8));
        this.expireHours = expireHours;
    }
    // 登陆成功后生成Token
    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(expireHours * 60 * 60);
        return Jwts.builder()
                // subject 通常放用户唯一标识
                .subject(String.valueOf(user.getId()))
                // 自定义字段：用户名
                .claim("username", user.getUsername())
                // 自定义字段：角色
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }
}
