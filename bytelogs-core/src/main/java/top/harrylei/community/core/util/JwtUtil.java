package top.harrylei.community.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import top.harrylei.community.api.enums.user.UserRoleEnum;
import top.harrylei.community.core.config.JwtProperties;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

/**
 * JWT 工具类
 *
 * @author harry
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * 获取密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * 生成JWT令牌
     *
     * @param userId    用户ID
     * @param role      用户角色
     * @param keepLogin 是否保持登录状态
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId, UserRoleEnum role, boolean keepLogin) {
        if (userId == null || role == null) {
            throw new IllegalArgumentException("用户ID和角色不能为空");
        }
        long now = System.currentTimeMillis();
        // 根据keepLogin选择过期时间
        Duration expireDuration = keepLogin ? jwtProperties.getKeepLoginExpire() : jwtProperties.getDefaultExpire();
        Date expiryDate = new Date(now + expireDuration.toMillis());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.getCode())
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从JWT令牌中解析用户ID
     *
     * @param token JWT令牌
     * @return 用户ID，无效令牌返回null
     */
    public Long extractUserId(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        try {
            String subject = Optional.ofNullable(parseClaims(token)).map(Claims::getSubject).orElse(null);
            if (StringUtils.isBlank(subject)) {
                return null;
            }
            return Long.valueOf(subject);
        } catch (NumberFormatException e) {
            log.warn("JWT令牌中用户ID格式错误: {}", token);
            return null;
        }
    }

    /**
     * 从JWT令牌中解析用户角色
     *
     * @param token JWT令牌
     * @return 用户角色，无效令牌返回null
     */
    public UserRoleEnum extractUserRole(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        Integer roleCode = Optional.ofNullable(parseClaims(token))
                .map(claims -> claims.get("role", Integer.class))
                .orElse(null);
        return UserRoleEnum.fromCode(roleCode);
    }

    /**
     * 检查JWT令牌是否已过期
     *
     * @param token JWT令牌
     * @return 是否已过期
     */
    public boolean isTokenExpired(String token) {
        if (StringUtils.isBlank(token)) {
            return true;
        }
        return getTokenExpiration(token).before(new Date());
    }

    /**
     * 获取JWT令牌的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getTokenExpiration(String token) {
        if (StringUtils.isBlank(token)) {
            return new Date(0);
        }
        return Optional.ofNullable(parseClaims(token)).map(Claims::getExpiration).orElse(new Date(0));
    }

    /**
     * 获取令牌过期秒数
     *
     * @return 过期秒数
     */
    public Duration getDefaultExpire() {
        return jwtProperties.getDefaultExpire();
    }

    /**
     * 获取保持登录状态的令牌过期时间
     *
     * @return 保持登录状态的过期时间
     */
    public Duration getKeepLoginExpire() {
        return jwtProperties.getKeepLoginExpire();
    }

    /**
     * 解析JWT令牌中的所有声明
     *
     * @param token JWT令牌
     * @return 声明内容，解析失败返回null
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.warn("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }
}