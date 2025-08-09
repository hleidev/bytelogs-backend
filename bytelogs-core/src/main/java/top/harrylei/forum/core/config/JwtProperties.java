package top.harrylei.forum.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.time.Duration;

/**
 * JWT 配置参数
 *
 * @author harry
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
     * 默认有效期
     */
    private Duration defaultExpire;

    /**
     * 保持登录有效期
     */
    private Duration keepLoginExpire;
}