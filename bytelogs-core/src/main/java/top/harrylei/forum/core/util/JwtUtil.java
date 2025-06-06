package top.harrylei.forum.core.util;

import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;

/**
 * JWT 工具类
 * <p>
 * 用于生成和解析JWT令牌的工具类，支持自动配置和静态方法调用
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * JWT Bearer认证前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

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

    // 静态变量，用于存储初始化后的值
    private static SecretKey secretKey;
    private static String issuerStatic;
    private static Long expireSecondsStatic;

    /**
     * 初始化JWT配置
     * <p>
     * Spring容器启动后自动调用，将配置属性设置到静态变量中
     * </p>
     */
    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        issuerStatic = issuer;
        expireSecondsStatic = expire;
        log.info("JWT工具类初始化完成，issuer={}, expireSeconds={}", issuer, expire);
    }

    /**
     * 生成JWT令牌
     *
     * @param userId 用户ID
     * @param role 用户角色
     * @return JWT令牌字符串
     */
    public static String generateToken(Long userId, UserRoleEnum role) {
        checkInit();
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + expireSecondsStatic * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())
                .setIssuer(issuerStatic)
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从JWT令牌中解析用户ID
     *
     * @param token JWT令牌
     * @return 用户ID，无效令牌返回null
     */
    public static Long parseUserId(String token) {
        checkInit();
        String subject = Optional.ofNullable(parseAllClaims(token)).map(Claims::getSubject).orElse(null);
        if (StringUtils.isBlank(subject)) {
            return null;
        }
        return Long.valueOf(subject);
    }

    /**
     * 从JWT令牌中解析用户角色
     *
     * @param token JWT令牌
     * @return 用户角色，无效令牌返回null
     */
    public static String parseRole(String token) {
        checkInit();
        return Optional.ofNullable(parseAllClaims(token)).map(claims -> claims.get("role", String.class)).orElse(null);
    }

    /**
     * 检查JWT令牌是否已过期
     *
     * @param token JWT令牌
     * @return 是否已过期
     */
    public static boolean isTokenExpired(String token) {
        checkInit();
        return getExpiration(token).before(new Date());
    }

    /**
     * 获取JWT令牌的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public static Date getExpiration(String token) {
        checkInit();
        return Optional.ofNullable(parseAllClaims(token)).map(Claims::getExpiration).orElse(new Date(0));
    }

    /**
     * 获取令牌过期秒数
     *
     * @return 过期秒数
     */
    public static Long getExpireSeconds() {
        checkInit();
        return expireSecondsStatic;
    }

    /**
     * 从HTTP Authorization头中提取JWT令牌
     *
     * @param authorizationHeader HTTP Authorization头的值
     * @return JWT令牌，无效格式返回null
     */
    public static String extractTokenFromAuthorizationHeader(String authorizationHeader) {
        if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        log.debug("Authorization header 无效或格式不正确: {}", authorizationHeader);
        return null;
    }

    /**
     * 解析JWT令牌中的所有声明
     *
     * @param token JWT令牌
     * @return 声明内容，解析失败返回null
     */
    private static Claims parseAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            log.warn("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查工具类是否已初始化
     */
    private static void checkInit() {
        if (secretKey == null) {
            throw new IllegalStateException("JWT工具类未初始化");
        }
    }
}