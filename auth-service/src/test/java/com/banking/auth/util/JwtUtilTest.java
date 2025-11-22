package com.banking.auth.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // Set test values using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtil, "secret", "banking-system-secret-key-for-jwt-token-generation-and-validation");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 hours
    }

    @Test
    void testGenerateToken_Success() {
        // Given
        String username = "john_doe";
        Long userId = 1L;
        String role = "CUSTOMER";

        // When
        String token = jwtUtil.generateToken(username, userId, role);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        String username = "john_doe";
        Long userId = 1L;
        String role = "CUSTOMER";
        String token = jwtUtil.generateToken(username, userId, role);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Given - Create a token with 0 expiration time (already expired)
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret", "banking-system-secret-key-for-jwt-token-generation-and-validation");
        ReflectionTestUtils.setField(expiredJwtUtil, "expiration", -1000L); // Negative expiration = expired
        
        String expiredToken = expiredJwtUtil.generateToken("john_doe", 1L, "CUSTOMER");

        // When
        boolean isValid = jwtUtil.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testExtractUsername_Success() {
        // Given
        String username = "john_doe";
        Long userId = 1L;
        String role = "CUSTOMER";
        String token = jwtUtil.generateToken(username, userId, role);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void testExtractUserId_Success() {
        // Given
        String username = "john_doe";
        Long userId = 1L;
        String role = "CUSTOMER";
        String token = jwtUtil.generateToken(username, userId, role);

        // When
        Long extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void testExtractRole_Success() {
        // Given
        String username = "john_doe";
        Long userId = 1L;
        String role = "CUSTOMER";
        String token = jwtUtil.generateToken(username, userId, role);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    void testExtractClaim_Success() {
        // Given
        String username = "john_doe";
        Long userId = 1L;
        String role = "CUSTOMER";
        String token = jwtUtil.generateToken(username, userId, role);

        // When
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);

        // Then
        assertThat(subject).isEqualTo(username);
    }

    @Test
    void testGenerateToken_DifferentRoles_Success() {
        // Test CUSTOMER role
        String customerToken = jwtUtil.generateToken("customer_user", 1L, "CUSTOMER");
        assertThat(jwtUtil.extractRole(customerToken)).isEqualTo("CUSTOMER");
        assertThat(jwtUtil.validateToken(customerToken)).isTrue();

        // Test TELLER role
        String tellerToken = jwtUtil.generateToken("teller_user", 2L, "TELLER");
        assertThat(jwtUtil.extractRole(tellerToken)).isEqualTo("TELLER");
        assertThat(jwtUtil.validateToken(tellerToken)).isTrue();
    }

    @Test
    void testExtractExpiration_Success() {
        // Given
        String username = "john_doe";
        Long userId = 1L;
        String role = "CUSTOMER";
        String token = jwtUtil.generateToken(username, userId, role);

        // When
        var expirationDate = jwtUtil.extractExpiration(token);

        // Then
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate.getTime()).isGreaterThan(System.currentTimeMillis());
    }
}
