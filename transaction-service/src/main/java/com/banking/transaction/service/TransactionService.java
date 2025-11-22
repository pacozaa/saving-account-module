package com.banking.transaction.service;

import com.banking.transaction.client.AccountServiceClient;
import com.banking.transaction.dto.AccountDto;
import com.banking.transaction.dto.LogTransactionRequest;
import com.banking.transaction.dto.TransactionDto;
import com.banking.transaction.entity.Transaction;
import com.banking.transaction.exception.TransactionNotFoundException;
import com.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

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
    public List<TransactionDto> getTransactionsByAccountId(Long accountId, Long authenticatedUserId, String userRole) {
        log.info("Fetching transactions for account: {} by user: {} with role: {}", accountId, authenticatedUserId, userRole);
        
        // Only PERSON (customers) need ownership validation
        // TELLERs can view any transaction
        if ("PERSON".equals(userRole)) {
            validateAccountOwnership(accountId, authenticatedUserId);
        }
        
        List<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc(accountId);
        
        log.info("Found {} transactions for account: {}", transactions.size(), accountId);
        
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long transactionId, Long authenticatedUserId, String userRole) {
        log.info("Fetching transaction with ID: {} by user: {} with role: {}", transactionId, authenticatedUserId, userRole);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId));
        
        // Only PERSON (customers) need ownership validation
        if ("PERSON".equals(userRole)) {
            validateAccountOwnership(transaction.getAccountId(), authenticatedUserId);
        }
        
        return mapToDto(transaction);
    }

    private void validateAccountOwnership(Long accountId, Long authenticatedUserId) {
        try {
            AccountDto account = accountServiceClient.getAccountById(accountId.toString());
            
            if (!account.getUserId().equals(authenticatedUserId)) {
                log.warn("User {} attempted to access account {} owned by user {}", 
                        authenticatedUserId, accountId, account.getUserId());
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, 
                        "You are not authorized to access transactions for this account"
                );
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating account ownership: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to validate account ownership"
            );
        }
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
