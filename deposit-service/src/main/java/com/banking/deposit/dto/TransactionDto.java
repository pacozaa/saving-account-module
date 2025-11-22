package com.banking.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long accountId;
    private String type;
    private BigDecimal amount;
    private Long relatedAccountId;
    private String description;
    private LocalDateTime timestamp;
    private String status;
}
