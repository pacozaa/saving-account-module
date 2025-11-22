package com.banking.register.service;

import com.banking.register.client.AccountClient;
import com.banking.register.client.dto.AccountDto;
import com.banking.register.client.dto.CreateAccountRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
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
    private AccountClient accountClient;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @InjectMocks
    private RegisterService registerService;
    
    private RegisterRequest registerRequest;
    private User savedUser;
    private AccountDto accountDto;
    
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
        
        accountDto = AccountDto.builder()
                .id("100")
                .userId(1L)
                .accountNumber("ACC1001")
                .accountType("SAVINGS")
                .balance(BigDecimal.ZERO)
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
        when(accountClient.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(ResponseEntity.ok(accountDto));
        
        // When
        RegisterResponse response = registerService.registerUser(registerRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("john_doe");
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getUser().getPassword()).isEqualTo("$2a$10$hashedPassword");
        assertThat(response.getDefaultAccountId()).isEqualTo(100L);
        assertThat(response.getMessage()).contains("registered successfully");
        
        // Verify password was hashed
        verify(passwordEncoder).encode("password123");
        
        // Verify user was saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("john_doe");
        assertThat(capturedUser.getPassword()).isEqualTo("$2a$10$hashedPassword");
        
        // Verify account was created
        ArgumentCaptor<CreateAccountRequest> accountCaptor = ArgumentCaptor.forClass(CreateAccountRequest.class);
        verify(accountClient).createAccount(accountCaptor.capture());
        CreateAccountRequest capturedRequest = accountCaptor.getValue();
        assertThat(capturedRequest.getUserId()).isEqualTo(1L);
        assertThat(capturedRequest.getAccountType()).isEqualTo("SAVINGS");
        assertThat(capturedRequest.getInitialBalance()).isEqualTo(BigDecimal.ZERO);
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
        verify(accountClient, never()).createAccount(any());
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
        verify(accountClient, never()).createAccount(any());
    }
    
    @Test
    @DisplayName("Should create default account when registering user")
    void testRegisterUser_CreatesDefaultAccount() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(accountClient.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(ResponseEntity.ok(accountDto));
        
        // When
        RegisterResponse response = registerService.registerUser(registerRequest);
        
        // Then
        assertThat(response.getDefaultAccountId()).isNotNull();
        assertThat(response.getDefaultAccountId()).isEqualTo(100L);
        
        // Verify account client was called with correct parameters
        ArgumentCaptor<CreateAccountRequest> captor = ArgumentCaptor.forClass(CreateAccountRequest.class);
        verify(accountClient).createAccount(captor.capture());
        
        CreateAccountRequest request = captor.getValue();
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getAccountType()).isEqualTo("SAVINGS");
        assertThat(request.getInitialBalance()).isEqualByComparingTo(BigDecimal.ZERO);
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
        when(accountClient.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(ResponseEntity.ok(accountDto));
        
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
