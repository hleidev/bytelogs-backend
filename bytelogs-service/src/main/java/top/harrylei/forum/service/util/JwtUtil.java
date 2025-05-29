package top.harrylei.forum.service.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + jwtProperties.getExpire() * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token，返回用户 ID
     * 
     * @param token JWT令牌字符串
     * @return 用户ID，无效则返回null
     */
    public Long parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException e) {
            log.warn("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }
}