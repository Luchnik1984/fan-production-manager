package com.fanproduction.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${JWT_SECRET:mySecretKeyForJWTTokenGeneration12345678901234567890}")
    private String secret;

    @Value("${JWT_EXPIRATION:86400000}")
    private Long expiration;

    @Value("${JWT_REFRESH_EXPIRATION:604800000}")  // 7 дней в миллисекундах
    private Long refreshExpiration;

    @PostConstruct
    public void init() {
        System.out.println("=== JwtService initialized ===");
        System.out.println("JWT_SECRET: " + secret);
        System.out.println("JWT_EXPIRATION: " + expiration + " ms (" + (expiration / 1000 / 60 / 60) + " hours)");
        System.out.println("JWT_REFRESH_EXPIRATION: " + refreshExpiration + " ms (" + (refreshExpiration / 1000 / 60 / 60 / 24) + " days)");
        System.out.println("==============================");
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Генерация access токена (короткоживущий)
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Генерация refresh токена (долгоживущий)
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", "refresh")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {

        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {

        return (String) extractClaims(token).get("role");
    }

    public String extractType(String token) {
        return (String) extractClaims(token).get("type");
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();

            System.out.println("Token validation:");
            System.out.println("  Expiration: " + expiration);
            System.out.println("  Current time: " +new Date());
            System.out.println("  Is expired: " + expiration.before(new Date()));
            return !expiration.before(new Date());
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractType(token));
        } catch (Exception e) {
            return false;
        }
    }
}
