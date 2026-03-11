package com.wfh.aipassagecreator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * @Title: SecurityConfig
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.config
 * @Date 2026/3/11 10:23
 * @description: Spring Security 配置
 */
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 放行登录注册接口
                        .requestMatchers("/user/login", "/user/register").permitAll()
                        // 放行健康检查接口
                        .requestMatchers("/health").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 配置 SecurityContext 持久化
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository())
                )
                // 配置表单登录（可选，用于测试）
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .permitAll()
                )
                // 配置登出
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login")
                        .permitAll()
                );

        return http.build();
    }
}
