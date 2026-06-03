package com.example.inventory.config;

import com.example.inventory.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离接口一般关闭 CSRF
                // 因为我们不用浏览器 Session 表单登录，而是用 Authorization token
                .csrf(AbstractHttpConfigurer::disable)

                // 关闭默认表单登录页面
                .formLogin(AbstractHttpConfigurer::disable)

                // 关闭 HTTP Basic 登录弹窗
                // 现在我们已经准备使用 JWT，不再使用浏览器弹窗式登录
                .httpBasic(AbstractHttpConfigurer::disable)

                // 设置为无状态
                // 后端不保存登录 Session，每次请求都靠 token 识别用户
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // 健康检查和登录接口不需要 token
                        .requestMatchers("/api/health", "/api/auth/login").permitAll()

                        // 其他接口都需要登录
                        .anyRequest().authenticated()
                )

                // 把我们自己的 JWT 过滤器加入 Spring Security 过滤器链
                // 它会在 UsernamePasswordAuthenticationFilter 之前执行
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // 支持数据库里的 {noop}123456
        // 以后你改成 BCrypt 密码，也可以继续用这个 PasswordEncoder
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}