package com.banking.deposit.controller;

import com.banking.deposit.dto.DepositRequest;
import com.banking.deposit.dto.DepositResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/deposit")
@Tag(name = "Deposit Operations", description = "Endpoints for deposit orchestration")
@SecurityRequirement(name = "bearerAuth")
public class DepositController {

    @Operation(
            summary = "Make a Deposit",
            description = "Deposits money into a specified account. " +
                    "Coordinates with Account Service to update balance and Transaction Service to log the transaction."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deposit successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepositResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PostMapping
    public ResponseEntity<DepositResponse> deposit(
            @Valid @RequestBody DepositRequest request
    ) {
        // TODO: Implement actual orchestration logic
        // 1. Validate account exists (call Account Service)
        // 2. Update account balance (call Account Service)
        // 3. Log transaction (call Transaction Service)
        
        DepositResponse response = new DepositResponse(
                1001L,
                request.getAccountId(),
                request.getAmount(),
                new BigDecimal("2500.00"),
                "Deposit successful"
        );
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Health Check",
            description = "Returns the health status of the deposit service"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Deposit Service is running");
    }
}
