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
    /**
     * JWT颁发者标识
     */
    private String issuer;
    
    /**
     * JWT加密密钥
     */
    private String secret;
    
    /**
     * JWT令牌有效时间，单位：秒
     */
    private Long expire;
} 