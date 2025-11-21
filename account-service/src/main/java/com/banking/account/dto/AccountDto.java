package com.banking.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account information")
public class AccountDto {

    @Schema(description = "Account ID", example = "101")
    private Long id;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Account number", example = "1234567890")
    private String accountNumber;

    @Schema(description = "Account balance", example = "1500.00")
    private BigDecimal balance;

    @Schema(description = "Account status", example = "ACTIVE", allowableValues = {"ACTIVE", "SUSPENDED", "CLOSED"})
    private String status;

    @Schema(description = "Account type", example = "SAVINGS", allowableValues = {"SAVINGS", "CHECKING"})
    private String accountType;
}
