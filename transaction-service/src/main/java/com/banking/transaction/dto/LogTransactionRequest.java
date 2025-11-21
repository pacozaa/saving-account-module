package com.banking.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to log a new transaction")
public class LogTransactionRequest {

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "101")
    private Long accountId;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "Transaction type", example = "DEPOSIT", allowableValues = {"DEPOSIT", "WITHDRAWAL", "TRANSFER"})
    private String transactionType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Transaction amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Related account ID (for transfers)", example = "102")
    private Long relatedAccountId;

    @Schema(description = "Transaction description", example = "Deposit from teller")
    private String description;
}
