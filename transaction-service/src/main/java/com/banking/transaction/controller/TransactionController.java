package com.banking.transaction.controller;

import com.banking.transaction.dto.LogTransactionRequest;
import com.banking.transaction.dto.TransactionDto;
import com.banking.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "Endpoints for transaction history and logging")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Get Transactions by Account",
            description = "Retrieves transaction history for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction history retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionDto>> getTransactionsByAccount(
            @Parameter(description = "Account ID", example = "101")
            @PathVariable Long accountId,
            @Parameter(description = "6-digit PIN for validation", example = "123456", required = true)
            @RequestParam String pin,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        List<TransactionDto> transactions = transactionService.getTransactionsByAccountId(accountId, pin, authenticatedUserId, userRole);
        return ResponseEntity.ok(transactions);
    }

    @Operation(
            summary = "Get Transaction by ID",
            description = "Retrieves a specific transaction by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Transaction found")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getTransaction(
            @Parameter(description = "Transaction ID", example = "1001")
            @PathVariable Long transactionId,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        TransactionDto transaction = transactionService.getTransactionById(transactionId, authenticatedUserId, userRole);
        return ResponseEntity.ok(transaction);
    }

    @Operation(
            summary = "Log Transaction",
            description = "Records a new transaction (used internally by orchestrator services)"
    )
    @ApiResponse(responseCode = "201", description = "Transaction logged successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping
    public ResponseEntity<TransactionDto> logTransaction(
            @Valid @RequestBody LogTransactionRequest request
    ) {
        TransactionDto transaction = transactionService.logTransaction(request);
        return ResponseEntity.status(201).body(transaction);
    }
}
