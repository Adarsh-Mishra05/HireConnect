package com.hireconnect.apigateway.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSignKey() {
        log.debug("Generating JWT signing key for token validation");
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        log.debug("Extracting all claims from JWT token");
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        String email = extractAllClaims(token).getSubject();
        log.debug("Extracted email from token: {}", email);
        return email;
    }

    public Long extractUserId(String token) {
        Long userId = extractAllClaims(token).get("userId", Long.class);
        log.debug("Extracted userId from token: {}", userId);
        return userId;
    }

    public String extractRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        log.debug("Extracted role from token: {}", role);
        return role;
    }

    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            boolean isValid = expiration != null && expiration.after(new Date());
            log.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT token validation failed: {}", ex.getMessage());
            return false;
        }
    }
}
