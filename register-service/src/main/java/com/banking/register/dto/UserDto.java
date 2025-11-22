package com.banking.register.dto;

import com.banking.register.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information response")
public class UserDto {
    
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User role", example = "PERSON")
    private UserRole role;
    
    @Schema(description = "BCrypt hashed password")
    private String password;
    
    @Schema(description = "Registration timestamp", example = "2025-11-22T10:30:00")
    private LocalDateTime registeredAt;
}
