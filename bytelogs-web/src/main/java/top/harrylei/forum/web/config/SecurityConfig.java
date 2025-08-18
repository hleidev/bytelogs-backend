package top.harrylei.forum.web.config;

import lombok.RequiredArgsConstructor;
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
import top.harrylei.forum.web.filter.JwtAuthenticationFilter;
import top.harrylei.forum.web.security.CustomAccessDeniedHandler;
import top.harrylei.forum.web.security.CustomAuthenticationEntryPoint;

import java.util.List;

/**
 * Spring Security 安全配置类
 *
 * @author harry
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * 配置Spring Security过滤器链
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
                        .requestMatchers("/v1/auth/login").permitAll()
                        .requestMatchers("/v1/auth/register").permitAll()
                        .requestMatchers("/v1/admin/auth/login").permitAll()
                        .requestMatchers("/v1/tag/**").permitAll()
                        .requestMatchers("/v1/category/**").permitAll()
                        .requestMatchers("/v1/article/**").permitAll()
                        .requestMatchers("/v1/comment/**").permitAll()
                        .requestMatchers("/v1/rank/**").permitAll()
                        .requestMatchers("/v1/public/**").permitAll()
                        .requestMatchers("/v1/test/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // 静态资源
                        .requestMatchers("/static/**").permitAll()
                        // OPTIONS请求放行
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 需要管理员权限的接口
                        .requestMatchers("/v1/admin/**").hasRole("ADMIN")
                        // 其他请求需要认证
                        .anyRequest().authenticated())
                // 配置异常处理
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // 添加JWT过滤器，在UsernamePasswordAuthenticationFilter之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class).build();
    }

    /**
     * 配置CORS（跨域资源共享）规则
     * <p>
     * 定义允许的跨域请求源、方法、头信息等，支持前后端分离架构。 主要配置： - 允许所有来源的请求（生产环境可能需要限制） - 允许常用HTTP方法 - 允许常用请求头 -
     * 暴露Authorization响应头，使前端能获取到JWT令牌 - 预检请求结果缓存时间为1小时
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