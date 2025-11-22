package com.banking.register.client.dto;

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
public class AccountDto {
    
    private String id;
    private Long userId;
    private BigDecimal balance;
    private String accountType;
    private LocalDateTime createdAt;
}
