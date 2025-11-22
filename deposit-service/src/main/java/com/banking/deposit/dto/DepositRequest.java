package com.banking.deposit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Deposit request")
public class DepositRequest {

    @Schema(description = "Account ID to deposit into (7-digit account number)", example = "1234567", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Account ID is required")
    private String accountId;

    @Schema(description = "Deposit amount (minimum 1)", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    private BigDecimal amount;

    @Schema(description = "Teller ID processing the deposit", example = "5")
    private Long tellerId;

    @Schema(description = "Deposit description", example = "Cash deposit")
    private String description;
}
