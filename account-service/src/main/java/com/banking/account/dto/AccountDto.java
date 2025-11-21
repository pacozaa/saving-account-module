package com.banking.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Account information")
public class AccountDto {

    @Schema(description = "Account ID (7-digit account number)", example = "1234567")
    private String id;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Account balance", example = "1500.00")
    private BigDecimal balance;

    @Schema(description = "Account type", example = "SAVINGS", allowableValues = {"SAVINGS", "CHECKING"})
    private String accountType;

    @Schema(description = "Account creation timestamp", example = "2025-11-21T10:30:00")
    private LocalDateTime createdAt;
}
