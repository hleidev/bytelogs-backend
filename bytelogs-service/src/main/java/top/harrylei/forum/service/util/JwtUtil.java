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
import top.harrylei.forum.service.config.JwtProperties;

/**
 * JWT 工具类，负责生成与解析 Token
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * 生成 Token
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
     * 解析 Token，返回用户 ID（无效返回 null）
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
            return null;
        }
    }
}