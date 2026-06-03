package com.example.inventory.security;

import com.example.inventory.api.dto.LoginResponse;
import com.example.inventory.domain.AppUser;
import io.jsonwebtoken.Claims;
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
    // 解析 token，拿到里面的 Claims
    // Claims 可以理解成 token 的内容载体，比如 subject、username、role、过期时间等
    public Claims parseToken(String token) {
        return Jwts.parser()
                // 设置解析 token 时使用的签名密钥
                // 必须和 generateToken() 里签名用的是同一个密钥
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 从 token 中取出用户 ID
    public Long getUserId(String token) {
        Claims claims = parseToken(token);

        // 我们生成 token 时把 userId 放在了 subject 里面
        // 所以这里从 subject 取出来，再转成 Long
        return Long.valueOf(claims.getSubject());
    }
}
