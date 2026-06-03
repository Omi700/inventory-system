package com.example.inventory.security;

import com.example.inventory.domain.AppUser;
import com.example.inventory.repository.AppUserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AppUserRepository appUserRepository
    ) {
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 从请求头中读取 Authorization
        // 正常格式应该是：Authorization: Bearer xxxxx.yyyyy.zzzzz
        String authorization = request.getHeader("Authorization");

        // 2. 如果没有 Authorization，或者不是 Bearer token，就直接放行
        // 放行不代表接口一定能访问，后面 Spring Security 会判断是否需要登录
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 去掉前面的 "Bearer "，得到真正的 JWT 字符串
        String token = authorization.substring(7);

        try {
            // 4. 解析 token，取出 userId
            // 如果 token 被篡改、过期、格式错误，这里会抛异常
            Long userId = jwtService.getUserId(token);

            // 5. 根据 userId 查询数据库，确认用户现在仍然存在
            AppUser user = appUserRepository.findById(userId)
                    .orElse(null);

            // 6. 如果用户不存在，或者用户被禁用，就不设置登录状态
            if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
                filterChain.doFilter(request, response);
                return;
            }

            // 7. 把数据库用户转换成当前登录用户对象
            // 这个对象后面可以在 Controller 里拿到
            CurrentUser currentUser = new CurrentUser(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getRole()
            );

            // 8. 设置权限
            // Spring Security 的角色权限一般习惯加 ROLE_ 前缀
            // ADMIN 会变成 ROLE_ADMIN
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole())
            );

            // 9. 创建认证对象
            // 第一个参数 principal：当前用户对象
            // 第二个参数 credentials：密码凭证，这里已经登录成功，不需要放密码，所以传 null
            // 第三个参数 authorities：当前用户权限
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            currentUser,
                            null,
                            authorities
                    );

            // 10. 把认证对象放入 Spring Security 上下文
            // 从这一行开始，后面的 Controller 就会认为用户已经登录
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException e) {
            // token 无效、过期、被篡改时会进入这里
            // 这里不直接返回错误，而是清空登录状态后继续往下走
            // 如果访问的是需要登录的接口，Spring Security 后面会返回 401
            SecurityContextHolder.clearContext();
        }

        // 11. 继续执行后续过滤器和 Controller
        filterChain.doFilter(request, response);
    }
}