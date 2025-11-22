package com.banking.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction DTO for communication with Transaction Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private Long id;
    private Long accountId;
    private String transactionType;
    private BigDecimal amount;
    private Long relatedAccountId;
    private String description;
    private LocalDateTime timestamp;
}
