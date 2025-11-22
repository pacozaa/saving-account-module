package com.banking.register.service;

import com.banking.register.dto.RegisterRequest;
import com.banking.register.dto.RegisterResponse;
import com.banking.register.dto.UserDto;
import com.banking.register.entity.User;
import com.banking.register.exception.UserAlreadyExistsException;
import com.banking.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterService {
    
    private final UserRepository userRepository;
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
        
        // Validate unique citizen ID
        if (userRepository.existsByCitizenId(request.getCitizenId())) {
            log.warn("Citizen ID already exists: {}", request.getCitizenId());
            throw new UserAlreadyExistsException("Citizen ID '" + request.getCitizenId() + "' is already registered");
        }
        
        // Validate citizen ID format (13 digits)
        if (!request.getCitizenId().matches("\\d{13}")) {
            throw new IllegalArgumentException("Citizen ID must be exactly 13 digits");
        }
        
        // Validate PIN format (6 digits)
        if (!request.getPin().matches("\\d{6}")) {
            throw new IllegalArgumentException("PIN must be exactly 6 digits");
        }
        
        // Hash password and PIN with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String hashedPin = passwordEncoder.encode(request.getPin());
        
        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(hashedPassword)
                .email(request.getEmail())
                .citizenId(request.getCitizenId())
                .thaiName(request.getThaiName())
                .englishName(request.getEnglishName())
                .pin(hashedPin)
                .role(request.getRole())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        
        // Build response
        UserDto userDto = UserDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .password(savedUser.getPassword())
                .email(savedUser.getEmail())
                .citizenId(maskCitizenId(savedUser.getCitizenId()))
                .thaiName(savedUser.getThaiName())
                .englishName(savedUser.getEnglishName())
                .role(savedUser.getRole())
                .registeredAt(savedUser.getRegisteredAt())
                .build();
        
        return RegisterResponse.builder()
                .user(userDto)
                .defaultAccountId(null)
                .message("User registered successfully")
                .build();
    }
    
    private String maskCitizenId(String citizenId) {
        if (citizenId == null || citizenId.length() != 13) {
            return citizenId;
        }
        return citizenId.substring(0, 7) + "******";
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
                .citizenId(maskCitizenId(user.getCitizenId()))
                .thaiName(user.getThaiName())
                .englishName(user.getEnglishName())
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
                .citizenId(maskCitizenId(user.getCitizenId()))
                .thaiName(user.getThaiName())
                .englishName(user.getEnglishName())
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
    
    public boolean validatePin(Long userId, String pin) {
        log.info("Validating PIN for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.banking.register.exception.UserNotFoundException("User not found with ID: " + userId));
        
        boolean isValid = passwordEncoder.matches(pin, user.getPin());
        log.info("PIN validation result for user {}: {}", userId, isValid);
        
        return isValid;
    }
}
