package top.harrylei.forum.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * JWT 配置参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * 颁发者
     */
    private String issuer;

    /**
     * 签名密钥
     */
    private String secret;

    /**
     * 有效期（秒）
     */
    private Long expire;
}