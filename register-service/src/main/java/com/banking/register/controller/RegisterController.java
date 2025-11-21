package com.banking.register.controller;

import com.banking.register.dto.RegistrationRequest;
import com.banking.register.dto.RegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
@Tag(name = "User Registration", description = "User registration and onboarding endpoints")
public class RegisterController {

    @Operation(
            summary = "Register New User",
            description = "Creates a new user account with default banking account. " +
                    "Returns user information and account ID upon successful registration."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists"
            )
    })
    @PostMapping
    public ResponseEntity<RegistrationResponse> registerUser(
            @Valid @RequestBody RegistrationRequest request
    ) {
        // TODO: Implement actual registration logic
        // This is a sample implementation for demonstration
        
        RegistrationResponse response = new RegistrationResponse(
                1L,
                request.getUsername(),
                request.getEmail(),
                request.getRole(),
                101L,
                "User registered successfully"
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Check Username Availability",
            description = "Checks if a username is available for registration"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Username availability checked"
            )
    })
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(
            @PathVariable String username
    ) {
        // TODO: Implement username check logic
        return ResponseEntity.ok().body("{\"available\": true}");
    }
}
