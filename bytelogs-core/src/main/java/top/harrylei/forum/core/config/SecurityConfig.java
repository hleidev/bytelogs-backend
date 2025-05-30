package top.harrylei.forum.core.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import top.harrylei.forum.core.security.JwtAuthenticationFilter;

/**
 * Spring Security 安全配置类
 * <p>
 * 提供Web应用安全配置，包括：
 * - JWT令牌认证
 * - 接口权限控制
 * - CORS跨域配置
 * - 无状态会话管理
 * </p>
 * <p>
 * 该配置确保系统安全性，同时支持前后端分离架构。
 * 通过EnableMethodSecurity注解，支持@PreAuthorize等方法级安全注解。
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置Spring Security过滤器链
     * <p>
     * 定义系统安全策略，包括：
     * - 禁用CSRF（跨站请求伪造）保护，因为使用JWT进行无状态认证
     * - 启用CORS（跨域资源共享）支持
     * - 配置无状态会话管理，不使用Session
     * - 配置接口访问权限规则
     * - 添加JWT认证过滤器处理令牌认证
     * </p>
     *
     * @param http HttpSecurity配置对象
     * @return 配置完成的SecurityFilterChain
     * @throws Exception 配置过程中可能的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // 禁用CSRF保护，因为我们使用JWT进行认证
            .csrf(AbstractHttpConfigurer::disable)
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 设置会话管理为无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 公共接口，无需认证
                .requestMatchers("/api/v1/auth/**").permitAll().requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/error").permitAll().requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Swagger/OpenAPI相关接口
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 静态资源
                .requestMatchers("/static/**").permitAll()
                // OPTIONS请求放行
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 需要管理员权限的接口
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // 其他请求需要认证
                .anyRequest().authenticated())
            // 添加JWT过滤器，在UsernamePasswordAuthenticationFilter之前执行
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class).build();
    }

    /**
     * 配置CORS（跨域资源共享）规则
     * <p>
     * 定义允许的跨域请求源、方法、头信息等，支持前后端分离架构。
     * 主要配置：
     * - 允许所有来源的请求（生产环境可能需要限制）
     * - 允许常用HTTP方法
     * - 允许常用请求头
     * - 暴露Authorization响应头，使前端能获取到JWT令牌
     * - 预检请求结果缓存时间为1小时
     * </p>
     *
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源（生产环境可能需要限制特定域名）
        configuration.setAllowedOrigins(List.of("*"));
        // 允许的HTTP方法
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        // 暴露的响应头（允许前端JS代码访问的响应头）
        configuration.setExposedHeaders(List.of("Authorization"));
        // 预检请求结果缓存时间（秒）
        configuration.setMaxAge(3600L);

        // 将配置应用于所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}