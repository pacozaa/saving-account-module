package com.banking.transaction.service;

import com.banking.transaction.dto.LogTransactionRequest;
import com.banking.transaction.dto.TransactionDto;
import com.banking.transaction.entity.Transaction;
import com.banking.transaction.exception.TransactionNotFoundException;
import com.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionDto logTransaction(LogTransactionRequest request) {
        log.info("Logging transaction for account: {}, type: {}, amount: {}", 
                request.getAccountId(), request.getTransactionType(), request.getAmount());

        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .relatedAccountId(request.getRelatedAccountId())
                .description(request.getDescription())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction logged successfully with ID: {}", savedTransaction.getId());

        return mapToDto(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByAccountId(Long accountId) {
        log.info("Fetching transactions for account: {}", accountId);
        
        List<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc(accountId);
        
        log.info("Found {} transactions for account: {}", transactions.size(), accountId);
        
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long transactionId) {
        log.info("Fetching transaction with ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId));
        
        return mapToDto(transaction);
    }

    private TransactionDto mapToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setAccountId(transaction.getAccountId());
        dto.setType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setRelatedAccountId(transaction.getRelatedAccountId());
        dto.setDescription(transaction.getDescription());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setStatus("COMPLETED");
        return dto;
    }
}
