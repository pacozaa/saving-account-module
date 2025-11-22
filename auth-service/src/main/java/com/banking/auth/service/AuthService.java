package com.banking.auth.service;

import com.banking.auth.client.UserClient;
import com.banking.auth.dto.LoginRequest;
import com.banking.auth.dto.LoginResponse;
import com.banking.auth.dto.UserDto;
import com.banking.auth.exception.InvalidCredentialsException;
import com.banking.auth.exception.UserNotFoundException;
import com.banking.auth.util.JwtUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Authenticate user and generate JWT token
     * 
     * @param loginRequest Login credentials
     * @return LoginResponse containing JWT token and user information
     * @throws UserNotFoundException if user doesn't exist
     * @throws InvalidCredentialsException if password is incorrect
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for username: {}", loginRequest.getUsername());
        
        try {
            // Get user from Register Service
            UserDto user = userClient.getUserByUsername(loginRequest.getUsername());
            
            if (user == null) {
                log.warn("User not found: {}", loginRequest.getUsername());
                throw new UserNotFoundException("User not found: " + loginRequest.getUsername());
            }
            
            // Validate password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("Invalid password for user: {}", loginRequest.getUsername());
                throw new InvalidCredentialsException("Invalid username or password");
            }
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
            
            log.info("Login successful for user: {}", loginRequest.getUsername());
            
            return new LoginResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getRole()
            );
            
        } catch (FeignException.NotFound e) {
            log.warn("User not found in Register Service: {}", loginRequest.getUsername());
            throw new UserNotFoundException("User not found: " + loginRequest.getUsername());
        } catch (FeignException e) {
            log.error("Error communicating with Register Service: {}", e.getMessage());
            throw new RuntimeException("Unable to authenticate user at this time");
        }
    }

    /**
     * Validate JWT token
     * 
     * @param token JWT token to validate
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * Extract user ID from token
     * 
     * @param token JWT token
     * @return User ID
     */
    public Long extractUserId(String token) {
        return jwtUtil.extractUserId(token);
    }

    /**
     * Extract username from token
     * 
     * @param token JWT token
     * @return Username
     */
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    /**
     * Extract role from token
     * 
     * @param token JWT token
     * @return User role
     */
    public String extractRole(String token) {
        return jwtUtil.extractRole(token);
    }
}
