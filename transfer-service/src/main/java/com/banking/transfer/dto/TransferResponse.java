package com.banking.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transfer response")
public class TransferResponse {

    @Schema(description = "Transaction ID", example = "1002")
    private Long transactionId;

    @Schema(description = "Source account ID", example = "101")
    private Long fromAccountId;

    @Schema(description = "Destination account ID", example = "102")
    private Long toAccountId;

    @Schema(description = "Transferred amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Source account new balance", example = "1000.00")
    private BigDecimal fromAccountNewBalance;

    @Schema(description = "Destination account new balance", example = "1500.00")
    private BigDecimal toAccountNewBalance;

    @Schema(description = "Status message", example = "Transfer successful")
    private String message;
}
