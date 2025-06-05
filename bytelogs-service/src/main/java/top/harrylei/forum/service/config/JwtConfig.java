package top.harrylei.forum.service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import top.harrylei.forum.core.util.JwtUtil;

/**
 * JWT配置类，负责初始化JWT工具类
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtProperties jwtProperties;

    @PostConstruct
    public void init() {
        JwtUtil.init(jwtProperties.getSecret(), jwtProperties.getIssuer(), jwtProperties.getExpire());
    }
}