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

    @Schema(description = "Account ID to deposit into", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Account ID is required")
    private Long accountId;

    @Schema(description = "Deposit amount", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Schema(description = "Teller ID processing the deposit", example = "5")
    private Long tellerId;

    @Schema(description = "Deposit description", example = "Cash deposit")
    private String description;
}
