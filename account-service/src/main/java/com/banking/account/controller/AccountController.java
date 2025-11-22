package com.banking.account.controller;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.dto.UpdateBalanceRequest;
import com.banking.account.service.AccountService;
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

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {
    
    private final AccountService accountService;
    
    @PostMapping("/create")
    @Operation(summary = "Create a new account", description = "Creates a new bank account for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<AccountDto> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /api/accounts/create - userId: {}, accountType: {}", 
                request.getUserId(), request.getAccountType());
        
        AccountDto account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieves account details by account ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not account owner or invalid role"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> getAccount(
            @Parameter(description = "Account ID (7-digit account number)", example = "1234567")
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        log.info("GET /api/accounts/{} - authenticatedUserId: {}, userRole: {}", id, authenticatedUserId, userRole);
        
        AccountDto account = accountService.getAccount(id, authenticatedUserId);
        return ResponseEntity.ok(account);
    }
    
    @PutMapping("/{id}/balance")
    @Operation(summary = "Update account balance", 
               description = "Updates account balance by adding the specified amount (use negative value to deduct)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balance updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> updateBalance(
            @Parameter(description = "Account ID (7-digit account number)", example = "1234567")
            @PathVariable String id,
            @Valid @RequestBody UpdateBalanceRequest request) {
        log.info("PUT /api/accounts/{}/balance - amount: {}", id, request.getAmount());
        
        AccountDto account = accountService.updateBalance(id, request.getAmount());
        return ResponseEntity.ok(account);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get accounts by user ID", description = "Retrieves all accounts for a specific user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - cannot access other users' accounts or invalid role")
    })
    public ResponseEntity<List<AccountDto>> getAccountsByUserId(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        log.info("GET /api/accounts/user/{} - authenticatedUserId: {}, userRole: {}", userId, authenticatedUserId, userRole);
        
        List<AccountDto> accounts = accountService.getAccountsByUserId(userId, authenticatedUserId);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Simple health check endpoint")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Account Service is running");
    }
}
