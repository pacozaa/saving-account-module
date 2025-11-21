package com.banking.account.controller;

import com.banking.account.dto.AccountDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Account Management", description = "Endpoints for managing customer accounts and balances")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    @Operation(
            summary = "Get Account by ID",
            description = "Retrieves account details by account ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            )
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(
            @Parameter(description = "Account ID", example = "101")
            @PathVariable Long accountId
    ) {
        // TODO: Implement actual logic
        AccountDto account = new AccountDto(
                accountId,
                1L,
                "1234567890",
                new BigDecimal("1500.00"),
                "ACTIVE",
                "SAVINGS"
        );
        return ResponseEntity.ok(account);
    }

    @Operation(
            summary = "Get Accounts by User ID",
            description = "Retrieves all accounts belonging to a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of user accounts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDto>> getAccountsByUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId
    ) {
        // TODO: Implement actual logic
        AccountDto account = new AccountDto(
                101L,
                userId,
                "1234567890",
                new BigDecimal("1500.00"),
                "ACTIVE",
                "SAVINGS"
        );
        return ResponseEntity.ok(List.of(account));
    }

    @Operation(
            summary = "Update Account Balance",
            description = "Updates account balance (used internally by other services)"
    )
    @ApiResponse(responseCode = "200", description = "Balance updated successfully")
    @PutMapping("/{accountId}/balance")
    public ResponseEntity<Void> updateBalance(
            @Parameter(description = "Account ID", example = "101")
            @PathVariable Long accountId,
            @Parameter(description = "New balance amount", example = "2000.00")
            @RequestParam BigDecimal amount
    ) {
        // TODO: Implement actual logic
        return ResponseEntity.ok().build();
    }
}
