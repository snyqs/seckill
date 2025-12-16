package com.seckill.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT生成/验证工具
 */
@Slf4j
@Component
public class JwtUtil {
    /**
     * 密钥（生产环境需配置在配置文件）
     */
    @Value("${jwt.secret:seckill-system-2025-secret-key-123456}")
    private String secret;

    /**
     * 过期时间：2小时（单位：毫秒）
     */
    @Value("${jwt.expire:7200000}")
    private Long expire;

    /**
     * 生成token
     */
    public String generateToken(Long userId, String username, Integer role) {
        // 构建Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        // 生成密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        // 生成token
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(key)
                .compact();
    }

    /**
     * 解析token获取Claims
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析token失败：", e);
            return null;
        }
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        return claims != null && claims.getExpiration().after(new Date());
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 从token中获取用户角色
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", Integer.class) : null;
    }
}