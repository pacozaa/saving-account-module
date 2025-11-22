package com.banking.register.service;

import com.banking.register.dto.RegisterRequest;
import com.banking.register.dto.RegisterResponse;
import com.banking.register.entity.User;
import com.banking.register.entity.UserRole;
import com.banking.register.exception.UserAlreadyExistsException;
import com.banking.register.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Register Service Tests")
class RegisterServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @InjectMocks
    private RegisterService registerService;
    
    private RegisterRequest registerRequest;
    private User savedUser;
    
    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("john_doe")
                .password("password123")
                .email("john.doe@example.com")
                .role(UserRole.PERSON)
                .build();
        
        savedUser = User.builder()
                .id(1L)
                .username("john_doe")
                .password("$2a$10$hashedPassword")
                .email("john.doe@example.com")
                .role(UserRole.PERSON)
                .registeredAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    @DisplayName("Should register user successfully and hash password")
    void testRegisterUser_Success_HashesPassword() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        RegisterResponse response = registerService.registerUser(registerRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("john_doe");
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getUser().getPassword()).isEqualTo("$2a$10$hashedPassword");
        assertThat(response.getDefaultAccountId()).isNull();
        assertThat(response.getMessage()).contains("registered successfully");
        
        // Verify password was hashed
        verify(passwordEncoder).encode("password123");
        
        // Verify user was saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("john_doe");
        assertThat(capturedUser.getPassword()).isEqualTo("$2a$10$hashedPassword");
    }
    
    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterUser_DuplicateUsername_ThrowsException() {
        // Given
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> registerService.registerUser(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username 'john_doe' is already taken");
        
        // Verify no user was saved
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterUser_DuplicateEmail_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> registerService.registerUser(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email 'john.doe@example.com' is already registered");
        
        // Verify no user was saved
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should not create account when registering user")
    void testRegisterUser_DoesNotCreateAccount() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        RegisterResponse response = registerService.registerUser(registerRequest);
        
        // Then
        assertThat(response.getDefaultAccountId()).isNull();
    }
    
    @Test
    @DisplayName("Should validate username uniqueness before email")
    void testRegisterUser_ChecksUsernameBeforeEmail() {
        // Given
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> registerService.registerUser(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username");
        
        // Verify email check was not performed
        verify(userRepository).existsByUsername("john_doe");
        verify(userRepository, never()).existsByEmail(anyString());
    }
    
    @Test
    @DisplayName("Should include all user information in response")
    void testRegisterUser_IncludesAllUserInformation() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        RegisterResponse response = registerService.registerUser(registerRequest);
        
        // Then
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getUsername()).isEqualTo("john_doe");
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(UserRole.PERSON);
        assertThat(response.getUser().getRegisteredAt()).isNotNull();
    }
}
