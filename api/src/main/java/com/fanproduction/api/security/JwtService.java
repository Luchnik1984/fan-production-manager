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

@Service
public class JwtService {

    @Value("${JWT_SECRET:mySecretKeyForJWTTokenGeneration12345678901234567890}")
    private String secret;

    @Value("${JWT_EXPIRATION:86400000}")
    private Long expiration;

    @PostConstruct
    public void init() {
        System.out.println("=== JwtService initialized ===");
        System.out.println("JWT_SECRET: " + secret);
        System.out.println("JWT_EXPIRATION: " + expiration);
        System.out.println("==============================");
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
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

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            System.out.println("Token validation:");
            System.out.println("  Expiration: " + expiration);
            System.out.println("  Current time: " + now);
            System.out.println("  Is expired: " + expiration.before(now));
            return !expiration.before(now);
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
