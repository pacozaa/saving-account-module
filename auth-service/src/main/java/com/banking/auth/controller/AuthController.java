package com.banking.auth.controller;

import com.banking.auth.dto.ErrorResponse;
import com.banking.auth.dto.LoginRequest;
import com.banking.auth.dto.LoginResponse;
import com.banking.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and JWT token management endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User Login",
            description = "Authenticates user credentials and returns a JWT token for subsequent API calls"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validate Token",
            description = "Validates a JWT token and returns token information if valid"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token is valid"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token is invalid or expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // Extract token from "Bearer <token>"
        String token = authorizationHeader.replace("Bearer ", "");
        
        if (authService.validateToken(token)) {
            Long userId = authService.extractUserId(token);
            String username = authService.extractUsername(token);
            String role = authService.extractRole(token);
            
            return ResponseEntity.ok(String.format(
                    "{\"valid\": true, \"userId\": %d, \"username\": \"%s\", \"role\": \"%s\"}",
                    userId, username, role
            ));
        }
        
        return ResponseEntity.status(401).body("{\"valid\": false, \"message\": \"Invalid or expired token\"}");
    }

    @Operation(
            summary = "Health Check",
            description = "Returns the health status of the authentication service"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service is healthy"
    )
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
