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
            SELECT id, username, password_hash, nickname, avatar_url, role, status, created_at, updated_at
            FROM app_user
            WHERE username = ?
            """;
        List<AppUser> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            AppUser user = new AppUser();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setNickname(rs.getString("nickname"));
            user.setAvatarUrl(rs.getString("avatar_url"));
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
        SELECT id, username, password_hash, nickname, avatar_url, role, status, created_at, updated_at
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
            user.setAvatarUrl(rs.getString("avatar_url"));
            user.setRole(rs.getString("role"));
            user.setStatus(rs.getInt("status"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            return user;
        }, id);

        // 因为 id 是主键，最多只会查到一条
        return users.stream().findFirst();
    }
    // 新增用户
// 这个方法只负责和数据库打交道，不做业务判断。
// 比如“用户名是否重复”“角色是否合法”，这些应该放在 Service 层。
    public AppUser save(AppUser user) {
        String sql = """
        INSERT INTO app_user (username, password_hash, nickname, avatar_url, role, status)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(
                sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getStatus()
        );

        // 插入后重新查询一次，拿到数据库生成的 id、created_at、updated_at
        return findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalStateException("用户新增后查询失败"));
    }

}
