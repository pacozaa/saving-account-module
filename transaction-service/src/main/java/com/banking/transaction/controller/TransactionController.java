package com.banking.transaction.controller;

import com.banking.transaction.dto.TransactionDto;
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
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "Endpoints for transaction history and logging")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

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
            @PathVariable Long accountId
    ) {
        // TODO: Implement actual logic
        TransactionDto transaction = new TransactionDto(
                1001L,
                accountId,
                "DEPOSIT",
                new BigDecimal("500.00"),
                null,
                "Deposit from teller",
                LocalDateTime.now(),
                "COMPLETED"
        );
        return ResponseEntity.ok(List.of(transaction));
    }

    @Operation(
            summary = "Get Transaction by ID",
            description = "Retrieves a specific transaction by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Transaction found")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getTransaction(
            @Parameter(description = "Transaction ID", example = "1001")
            @PathVariable Long transactionId
    ) {
        // TODO: Implement actual logic
        TransactionDto transaction = new TransactionDto(
                transactionId,
                101L,
                "DEPOSIT",
                new BigDecimal("500.00"),
                null,
                "Deposit from teller",
                LocalDateTime.now(),
                "COMPLETED"
        );
        return ResponseEntity.ok(transaction);
    }

    @Operation(
            summary = "Log Transaction",
            description = "Records a new transaction (used internally by orchestrator services)"
    )
    @ApiResponse(responseCode = "201", description = "Transaction logged successfully")
    @PostMapping
    public ResponseEntity<TransactionDto> logTransaction(
            @RequestBody TransactionDto transaction
    ) {
        // TODO: Implement actual logic
        transaction.setId(1001L);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("COMPLETED");
        return ResponseEntity.status(201).body(transaction);
    }
}
