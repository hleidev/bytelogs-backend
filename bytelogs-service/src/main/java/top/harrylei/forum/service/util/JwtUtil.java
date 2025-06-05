package top.harrylei.forum.service.util;

import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.service.config.JwtProperties;

/**
 * JWT 工具类，负责生成与解析 Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    /**
     * JWT Bearer认证前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 初始化JWT密钥
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        log.info("JWT工具类初始化完成");
    }

    /**
     * 生成 Token
     *
     * @param userId 用户ID
     * @param role   用户角色
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId, UserRoleEnum role) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + jwtProperties.getExpire() * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())
                .setIssuer(jwtProperties.getIssuer())
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
    public Long parseUserId(String token) {
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
    public String parseRole(String token) {
        return Optional.ofNullable(parseAllClaims(token)).map(claims -> claims.get("role", String.class)).orElse(null);
    }

    /**
     * 检查JWT令牌是否已过期
     *
     * @param token JWT令牌
     * @return 是否已过期
     */
    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    /**
     * 获取JWT令牌的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        return Optional.ofNullable(parseAllClaims(token)).map(Claims::getExpiration).orElse(new Date(0));
    }

    /**
     * 获取令牌过期秒数
     *
     * @return 过期秒数
     */
    public Long getExpireSeconds() {
        return jwtProperties.getExpire();
    }

    /**
     * 从HTTP Authorization头中提取JWT令牌
     *
     * @param authorizationHeader HTTP Authorization头的值
     * @return JWT令牌，无效格式返回null
     */
    public String extractTokenFromAuthorizationHeader(String authorizationHeader) {
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
    private Claims parseAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            log.warn("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }
} 