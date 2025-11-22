package com.banking.register.controller;

import com.banking.register.dto.RegisterRequest;
import com.banking.register.dto.RegisterResponse;
import com.banking.register.dto.UserDto;
import com.banking.register.service.RegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Registration", description = "APIs for user registration")
public class RegisterController {
    
    private final RegisterService registerService;
    
    @PostMapping
    @Operation(summary = "Register a new user", description = "Creates a new user with a default savings account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or user already exists")
    })
    public ResponseEntity<RegisterResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {
        log.info("POST /register - username: {}, email: {}, role: {}", 
                request.getUsername(), request.getEmail(), request.getRole());
        
        RegisterResponse response = registerService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/user/{username}")
    @Operation(summary = "Get user by username", description = "Retrieves user information by username")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> getUserByUsername(
            @Parameter(description = "Username", example = "john_doe")
            @PathVariable String username) {
        log.info("GET /register/user/{}", username);
        
        UserDto user = registerService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/user/id/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves user information by user ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId) {
        log.info("GET /register/user/id/{}", userId);
        
        UserDto user = registerService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/validate-pin")
    @Operation(summary = "Validate user PIN", description = "Validates if the provided PIN matches the user's stored PIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PIN validation result"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Boolean> validatePin(
            @Parameter(description = "User ID", example = "1")
            @RequestParam Long userId,
            @Parameter(description = "6-digit PIN", example = "123456")
            @RequestParam String pin) {
        log.info("POST /register/validate-pin - userId: {}", userId);
        
        boolean isValid = registerService.validatePin(userId, pin);
        return ResponseEntity.ok(isValid);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Register Service is running");
    }
}
