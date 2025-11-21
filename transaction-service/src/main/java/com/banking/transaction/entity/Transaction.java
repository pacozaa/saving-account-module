package com.banking.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long accountId;
    
    @Column(nullable = false, length = 20)
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column
    private Long relatedAccountId; // For transfers
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 500)
    private String description;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
