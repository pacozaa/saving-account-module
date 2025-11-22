package com.banking.transfer.dto;

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
@Schema(description = "Transfer request")
public class TransferRequest {

    @Schema(description = "Source account ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "From account ID is required")
    private Long fromAccountId;

    @Schema(description = "Destination account ID", example = "102", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "To account ID is required")
    private Long toAccountId;

    @Schema(description = "Transfer amount", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Schema(description = "6-digit PIN for authorization", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "PIN is required")
    private String pin;

    @Schema(description = "Transfer description", example = "Payment for services")
    private String description;
}
