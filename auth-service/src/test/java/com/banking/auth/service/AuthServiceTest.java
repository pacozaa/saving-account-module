package com.banking.auth.service;

import com.banking.auth.client.UserClient;
import com.banking.auth.dto.LoginRequest;
import com.banking.auth.dto.LoginResponse;
import com.banking.auth.dto.UserDto;
import com.banking.auth.exception.InvalidCredentialsException;
import com.banking.auth.exception.UserNotFoundException;
import com.banking.auth.util.JwtUtil;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;
    private UserDto testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        // Create test user with hashed password
        testUser = new UserDto();
        testUser.setId(1L);
        testUser.setUsername("john_doe");
        testUser.setPassword(passwordEncoder.encode("password123")); // Hash the password
        testUser.setEmail("john@example.com");
        testUser.setRole("CUSTOMER");

        // Create login request
        loginRequest = new LoginRequest("john_doe", "password123");
    }

    @Test
    void testLogin_ValidCredentials_ReturnsToken() {
        // Given
        String expectedToken = "jwt.token.here";
        
        when(userClient.getUserByUsername(loginRequest.getUsername())).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser.getUsername(), testUser.getId(), testUser.getRole()))
                .thenReturn(expectedToken);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);
        assertThat(response.getUserId()).isEqualTo(testUser.getId());
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(response.getRole()).isEqualTo(testUser.getRole());

        verify(userClient).getUserByUsername(loginRequest.getUsername());
        verify(jwtUtil).generateToken(testUser.getUsername(), testUser.getId(), testUser.getRole());
    }

    @Test
    void testLogin_InvalidPassword_ThrowsException() {
        // Given
        LoginRequest invalidRequest = new LoginRequest("john_doe", "wrongpassword");
        
        when(userClient.getUserByUsername(invalidRequest.getUsername())).thenReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> authService.login(invalidRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userClient).getUserByUsername(invalidRequest.getUsername());
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        // Given
        when(userClient.getUserByUsername(loginRequest.getUsername())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userClient).getUserByUsername(loginRequest.getUsername());
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void testLogin_FeignNotFound_ThrowsUserNotFoundException() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "/users/username/john_doe",
                Collections.emptyMap(), null, new RequestTemplate());
        
        FeignException.NotFound notFoundException = new FeignException.NotFound(
                "User not found", request, null, Collections.emptyMap());
        
        when(userClient.getUserByUsername(loginRequest.getUsername())).thenThrow(notFoundException);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userClient).getUserByUsername(loginRequest.getUsername());
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void testLogin_FeignException_ThrowsRuntimeException() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "/users/username/john_doe",
                Collections.emptyMap(), null, new RequestTemplate());
        
        FeignException feignException = new FeignException.InternalServerError(
                "Internal Server Error", request, null, Collections.emptyMap());
        
        when(userClient.getUserByUsername(loginRequest.getUsername())).thenThrow(feignException);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unable to authenticate user");

        verify(userClient).getUserByUsername(loginRequest.getUsername());
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void testValidateToken_Success() {
        // Given
        String token = "valid.jwt.token";
        when(jwtUtil.validateToken(token)).thenReturn(true);

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void testExtractUserId_Success() {
        // Given
        String token = "valid.jwt.token";
        Long expectedUserId = 1L;
        when(jwtUtil.extractUserId(token)).thenReturn(expectedUserId);

        // When
        Long userId = authService.extractUserId(token);

        // Then
        assertThat(userId).isEqualTo(expectedUserId);
        verify(jwtUtil).extractUserId(token);
    }

    @Test
    void testExtractUsername_Success() {
        // Given
        String token = "valid.jwt.token";
        String expectedUsername = "john_doe";
        when(jwtUtil.extractUsername(token)).thenReturn(expectedUsername);

        // When
        String username = authService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo(expectedUsername);
        verify(jwtUtil).extractUsername(token);
    }

    @Test
    void testExtractRole_Success() {
        // Given
        String token = "valid.jwt.token";
        String expectedRole = "CUSTOMER";
        when(jwtUtil.extractRole(token)).thenReturn(expectedRole);

        // When
        String role = authService.extractRole(token);

        // Then
        assertThat(role).isEqualTo(expectedRole);
        verify(jwtUtil).extractRole(token);
    }

    @Test
    void testLogin_DifferentRoles_Success() {
        // Test CUSTOMER role
        UserDto customerUser = new UserDto(1L, "customer_user", 
                passwordEncoder.encode("password123"), "customer@example.com", "CUSTOMER");
        LoginRequest customerRequest = new LoginRequest("customer_user", "password123");
        
        when(userClient.getUserByUsername("customer_user")).thenReturn(customerUser);
        when(jwtUtil.generateToken("customer_user", 1L, "CUSTOMER")).thenReturn("token_customer");

        LoginResponse customerResponse = authService.login(customerRequest);
        assertThat(customerResponse.getRole()).isEqualTo("CUSTOMER");

        // Test TELLER role
        UserDto tellerUser = new UserDto(2L, "teller_user", 
                passwordEncoder.encode("password123"), "teller@example.com", "TELLER");
        LoginRequest tellerRequest = new LoginRequest("teller_user", "password123");
        
        when(userClient.getUserByUsername("teller_user")).thenReturn(tellerUser);
        when(jwtUtil.generateToken("teller_user", 2L, "TELLER")).thenReturn("token_teller");

        LoginResponse tellerResponse = authService.login(tellerRequest);
        assertThat(tellerResponse.getRole()).isEqualTo("TELLER");
    }
}
