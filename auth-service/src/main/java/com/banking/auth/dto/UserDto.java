package com.banking.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information from Register Service")
public class UserDto {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "BCrypt hashed password")
    private String password;

    @Schema(description = "Email address", example = "john@example.com")
    private String email;

    @Schema(description = "User role", example = "CUSTOMER")
    private String role;
}
