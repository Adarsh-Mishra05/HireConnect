package com.hireconnect.apigateway.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SECRET = "default-secret-key-default-secret-key-123456";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    @Test
    void extractEmailAndUserIdAndRoleFromToken() {
        String token = createToken(42L, "test@example.com", "CANDIDATE", 10_000);

        assertEquals("test@example.com", jwtService.extractEmail(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("CANDIDATE", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertFalse(jwtService.isTokenValid("invalid.token.value"));
    }

    @Test
    void expiredTokenReturnsFalse() {
        String token = createToken(1L, "expired@example.com", "USER", -10_000);
        assertFalse(jwtService.isTokenValid(token));
    }

    private String createToken(long userId, String email, String role, long ttlMillis) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(key)
                .compact();
    }
}
