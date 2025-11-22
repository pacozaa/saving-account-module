package com.banking.register.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Registration response including user and account information")
public class RegisterResponse {
    
    @Schema(description = "User information")
    private UserDto user;
    
    @Schema(description = "Default account ID created for the user", example = "123")
    private Long defaultAccountId;
    
    @Schema(description = "Success message", example = "User registered successfully with default account")
    private String message;
}
