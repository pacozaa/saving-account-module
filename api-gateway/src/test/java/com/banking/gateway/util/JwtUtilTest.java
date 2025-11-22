package com.banking.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "mySecretKeyForJWTThatIsLongEnoughAndSecure12345678901234567890";
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Helper method to generate a valid test token
     */
    private String generateTestToken(String username, Long userId, String role, Date expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        String username = "testuser";
        Long userId = 1L;
        String role = "CUSTOMER";
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60); // 1 hour from now
        String token = generateTestToken(username, userId, role, expiration);

        // When
        boolean result = jwtUtil.validateToken(token);

        // Then
        assertTrue(result, "Token should be valid");
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Given - token expired 1 hour ago
        String username = "testuser";
        Long userId = 1L;
        String role = "CUSTOMER";
        Date expiration = new Date(System.currentTimeMillis() - 1000 * 60 * 60); // 1 hour ago
        String token = generateTestToken(username, userId, role, expiration);

        // When
        boolean result = jwtUtil.validateToken(token);

        // Then
        assertFalse(result, "Expired token should be invalid");
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean result = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(result, "Malformed token should be invalid");
    }

    @Test
    void testValidateToken_TokenWithWrongSignature_ReturnsFalse() {
        // Given - token signed with different key
        String wrongSecret = "differentSecretKeyThatIsAlsoLongEnoughAndSecure1234567890";
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("role", "CUSTOMER");

        String token = Jwts.builder()
                .claims(claims)
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(wrongKey)
                .compact();

        // When
        boolean result = jwtUtil.validateToken(token);

        // Then
        assertFalse(result, "Token with wrong signature should be invalid");
    }

    @Test
    void testExtractUsername_Success() {
        // Given
        String expectedUsername = "john.doe";
        Long userId = 1L;
        String role = "CUSTOMER";
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        String token = generateTestToken(expectedUsername, userId, role, expiration);

        // When
        String actualUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }

    @Test
    void testExtractUserId_Success() {
        // Given
        String username = "testuser";
        Long expectedUserId = 12345L;
        String role = "TELLER";
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        String token = generateTestToken(username, expectedUserId, role, expiration);

        // When
        Long actualUserId = jwtUtil.extractUserId(token);

        // Then
        assertThat(actualUserId).isEqualTo(expectedUserId);
    }

    @Test
    void testExtractRole_Success() {
        // Given
        String username = "testuser";
        Long userId = 1L;
        String expectedRole = "ADMIN";
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        String token = generateTestToken(username, userId, expectedRole, expiration);

        // When
        String actualRole = jwtUtil.extractRole(token);

        // Then
        assertThat(actualRole).isEqualTo(expectedRole);
    }

    @Test
    void testExtractExpiration_Success() {
        // Given
        String username = "testuser";
        Long userId = 1L;
        String role = "CUSTOMER";
        Date expectedExpiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        String token = generateTestToken(username, userId, role, expectedExpiration);

        // When
        Date actualExpiration = jwtUtil.extractExpiration(token);

        // Then
        long timeDiff = Math.abs(actualExpiration.getTime() - expectedExpiration.getTime());
        assertTrue(timeDiff < 1000, "Expiration times should be within 1 second");
    }

    @Test
    void testExtractClaim_CustomClaim_Success() {
        // Given
        String username = "testuser";
        Long userId = 1L;
        String role = "CUSTOMER";
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        String token = generateTestToken(username, userId, role, expiration);

        // When
        Long extractedUserId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void testValidateToken_NullToken_ReturnsFalse() {
        // Given
        String token = null;

        // When
        boolean result = jwtUtil.validateToken(token);

        // Then
        assertFalse(result, "Null token should be invalid");
    }

    @Test
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Given
        String token = "";

        // When
        boolean result = jwtUtil.validateToken(token);

        // Then
        assertFalse(result, "Empty token should be invalid");
    }
}
