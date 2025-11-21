package com.banking.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction record")
public class TransactionDto {

    @Schema(description = "Transaction ID", example = "1001")
    private Long id;

    @Schema(description = "Account ID", example = "101")
    private Long accountId;

    @Schema(description = "Transaction type", example = "DEPOSIT", allowableValues = {"DEPOSIT", "WITHDRAWAL", "TRANSFER"})
    private String type;

    @Schema(description = "Transaction amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Related account ID (for transfers)", example = "102")
    private Long relatedAccountId;

    @Schema(description = "Transaction description", example = "Deposit from teller")
    private String description;

    @Schema(description = "Transaction timestamp", example = "2023-12-01T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Transaction status", example = "COMPLETED", allowableValues = {"PENDING", "COMPLETED", "FAILED"})
    private String status;
}
