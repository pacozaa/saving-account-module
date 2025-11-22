package com.banking.transaction.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDto {
    private String id;
    private Long userId;
    private BigDecimal balance;
    private String accountType;
    private LocalDateTime createdAt;
}
