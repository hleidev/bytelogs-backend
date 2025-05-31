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
import top.harrylei.forum.api.model.enums.RoleEnum;
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
    public String generateToken(Long userId, Integer role) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + jwtProperties.getExpire() * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", RoleEnum.of(role))
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long parseUserId(String token) {
        String subject = Optional.ofNullable(parseAllClaims(token)).map(Claims::getSubject).orElse(null);
        if (StringUtils.isBlank(subject)) {
            return null;
        }
        return Long.valueOf(subject);
    }

    public String parseRole(String token) {
        return Optional.ofNullable(parseAllClaims(token)).map(claims -> claims.get("role", String.class)).orElse(null);
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public Long getExpireSeconds() {
        return jwtProperties.getExpire();
    }

    public Date getExpiration(String token) {
        return Optional.ofNullable(parseAllClaims(token)).map(Claims::getExpiration).orElse(new Date(0));
    }

    private Claims parseAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            log.warn("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }
} 