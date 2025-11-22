package com.banking.register.service;

import com.banking.register.client.AccountClient;
import com.banking.register.client.dto.AccountDto;
import com.banking.register.client.dto.CreateAccountRequest;
import com.banking.register.dto.RegisterRequest;
import com.banking.register.dto.RegisterResponse;
import com.banking.register.dto.UserDto;
import com.banking.register.entity.User;
import com.banking.register.exception.UserAlreadyExistsException;
import com.banking.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterService {
    
    private final UserRepository userRepository;
    private final AccountClient accountClient;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        // Validate unique username
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username '" + request.getUsername() + "' is already taken");
        }
        
        // Validate unique email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email '" + request.getEmail() + "' is already registered");
        }
        
        // Hash password with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(hashedPassword)
                .email(request.getEmail())
                .role(request.getRole())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        
        // Create default account via Account Service
        CreateAccountRequest accountRequest = CreateAccountRequest.builder()
                .userId(savedUser.getId())
                .accountType("SAVINGS")
                .initialBalance(BigDecimal.ZERO)
                .build();
        
        ResponseEntity<AccountDto> accountResponse = accountClient.createAccount(accountRequest);
        AccountDto account = accountResponse.getBody();
        
        log.info("Default account created with ID: {} for user: {}", 
                account != null ? account.getId() : "null", savedUser.getId());
        
        // Build response
        UserDto userDto = UserDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .password(savedUser.getPassword())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .registeredAt(savedUser.getRegisteredAt())
                .build();
        
        return RegisterResponse.builder()
                .user(userDto)
                .defaultAccountId(account != null ? Long.parseLong(account.getId()) : null)
                .message("User registered successfully with default account")
                .build();
    }
    
    public UserDto getUserByUsername(String username) {
        log.info("Getting user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.banking.register.exception.UserNotFoundException("User not found: " + username));
        
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .role(user.getRole())
                .registeredAt(user.getRegisteredAt())
                .build();
    }
    
    public UserDto getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.banking.register.exception.UserNotFoundException("User not found with ID: " + userId));
        
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .role(user.getRole())
                .registeredAt(user.getRegisteredAt())
                .build();
    }
    
    public boolean validateCredentials(String username, String password) {
        log.info("Validating credentials for username: {}", username);
        
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }
}
