package top.harrylei.forum.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置类
 * 
 * 当前配置为开发阶段的临时配置，允许所有请求通过。
 * 在生产环境中，应根据实际业务需求配置适当的安全策略。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     * 
     * @param http HTTP安全配置对象
     * @return 配置完成的安全过滤链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用CSRF保护
        http.csrf(AbstractHttpConfigurer::disable)
            // 配置请求认证规则
            .authorizeHttpRequests(authorize -> authorize
                // 允许所有请求
                .anyRequest().permitAll());

        return http.build();
    }
}