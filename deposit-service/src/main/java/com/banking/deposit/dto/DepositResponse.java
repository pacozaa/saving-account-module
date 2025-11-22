package com.banking.deposit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Deposit response")
public class DepositResponse {

    @Schema(description = "Transaction ID", example = "1001")
    private Long transactionId;

    @Schema(description = "Account ID (7-digit account number)", example = "1234567")
    private String accountId;

    @Schema(description = "Deposited amount", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "New account balance", example = "2500.00")
    private BigDecimal newBalance;

    @Schema(description = "Status message", example = "Deposit successful")
    private String message;
}
