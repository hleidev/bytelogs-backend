package top.harrylei.forum.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.core.config.JwtProperties;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

/**
 * JWT 工具类
 *
 * @author harry
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final Logger log = LoggerFactory.getLogger(JwtUtil.class);

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
        checkInit();
        long now = System.currentTimeMillis();
        // 根据keepLogin选择过期时间
        Duration expireDuration = keepLogin ? jwtProperties.getKeepLoginExpire() : jwtProperties.getExpire();
        Date expiryDate = new Date(now + expireDuration.toMillis());
        return Jwts.builder().setSubject(String.valueOf(userId))
                .claim("role", role.name()).setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date(now)).setExpiration(expiryDate).signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从JWT令牌中解析用户ID
     *
     * @param token JWT令牌
     * @return 用户ID，无效令牌返回null
     */
    public Long parseUserId(String token) {
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
    public String parseRole(String token) {
        checkInit();
        return Optional.ofNullable(parseAllClaims(token)).map(claims -> claims.get("role", String.class)).orElse(null);
    }

    /**
     * 检查JWT令牌是否已过期
     *
     * @param token JWT令牌
     * @return 是否已过期
     */
    public boolean isTokenExpired(String token) {
        checkInit();
        return getExpiration(token).before(new Date());
    }

    /**
     * 获取JWT令牌的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        checkInit();
        return Optional.ofNullable(parseAllClaims(token)).map(Claims::getExpiration).orElse(new Date(0));
    }

    /**
     * 获取令牌过期秒数
     *
     * @return 过期秒数
     */
    public Long getExpireSeconds() {
        checkInit();
        return jwtProperties.getExpire().getSeconds();
    }

    /**
     * 解析JWT令牌中的所有声明
     *
     * @param token JWT令牌
     * @return 声明内容，解析失败返回null
     */
    private Claims parseAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            log.warn("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查工具类是否已初始化
     */
    private void checkInit() {
        getSecretKey();
    }
}