package top.harrylei.forum.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置类，读取 application.yml 中的 jwt 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String issuer;
    private String secret;         // 加密密钥
    private Long expire;   // 有效时间（秒）
}