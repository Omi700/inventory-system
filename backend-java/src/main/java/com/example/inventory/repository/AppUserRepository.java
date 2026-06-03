package com.example.inventory.repository;

import com.example.inventory.domain.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AppUserRepository {
    private final JdbcTemplate jdbcTemplate;
    public AppUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    //根据用户名查询用户
    public Optional<AppUser> findByUsername(String username) {
        String sql = """
            SELECT id, username, password_hash, nickname, role, status, created_at, updated_at
            FROM app_user
            WHERE username = ?
            """;
        List<AppUser> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            AppUser user = new AppUser();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setNickname(rs.getString("nickname"));
            user.setRole(rs.getString("role"));
            user.setStatus(rs.getInt("status"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return user;
        },username);
        return users.stream().findFirst();
    }
    // 根据用户 ID 查询用户，解析 JWT 后获取当前用户时使用
    public Optional<AppUser> findById(Long id) {
        String sql = """
        SELECT id, username, password_hash, nickname, role, status, created_at, updated_at
        FROM app_user
        WHERE id = ?
        """;

        List<AppUser> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            AppUser user = new AppUser();

            // 把数据库字段映射到 Java 对象
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setNickname(rs.getString("nickname"));
            user.setRole(rs.getString("role"));
            user.setStatus(rs.getInt("status"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            return user;
        }, id);

        // 因为 id 是主键，最多只会查到一条
        return users.stream().findFirst();
    }
}
