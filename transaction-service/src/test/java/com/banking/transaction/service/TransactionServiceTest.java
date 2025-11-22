package com.banking.transaction.service;

import com.banking.transaction.dto.LogTransactionRequest;
import com.banking.transaction.dto.TransactionDto;
import com.banking.transaction.entity.Transaction;
import com.banking.transaction.exception.TransactionNotFoundException;
import com.banking.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service Unit Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private com.banking.transaction.client.AccountServiceClient accountServiceClient;

    @Mock
    private com.banking.transaction.client.RegisterServiceClient registerServiceClient;

    @InjectMocks
    private TransactionService transactionService;

    private LogTransactionRequest logRequest;
    private Transaction transaction;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Setup log transaction request
        logRequest = new LogTransactionRequest();
        logRequest.setAccountId(101L);
        logRequest.setTransactionType("DEPOSIT");
        logRequest.setAmount(new BigDecimal("500.00"));
        logRequest.setRelatedAccountId(null);
        logRequest.setDescription("Test deposit");

        // Setup transaction entity
        transaction = Transaction.builder()
                .id(1001L)
                .accountId(101L)
                .transactionType("DEPOSIT")
                .amount(new BigDecimal("500.00"))
                .relatedAccountId(null)
                .description("Test deposit")
                .timestamp(now)
                .build();
    }

    @Test
    @DisplayName("Should log transaction successfully")
    void testLogTransaction_Success() {
        // Given
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionDto result = transactionService.logTransaction(logRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getAccountId()).isEqualTo(101L);
        assertThat(result.getType()).isEqualTo("DEPOSIT");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getDescription()).isEqualTo("Test deposit");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getTimestamp()).isEqualTo(now);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should log transfer transaction with related account")
    void testLogTransaction_TransferWithRelatedAccount() {
        // Given
        logRequest.setTransactionType("TRANSFER");
        logRequest.setRelatedAccountId(102L);
        logRequest.setDescription("Transfer to account 102");

        Transaction transferTransaction = Transaction.builder()
                .id(1002L)
                .accountId(101L)
                .transactionType("TRANSFER")
                .amount(new BigDecimal("500.00"))
                .relatedAccountId(102L)
                .description("Transfer to account 102")
                .timestamp(now)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transferTransaction);

        // When
        TransactionDto result = transactionService.logTransaction(logRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("TRANSFER");
        assertThat(result.getRelatedAccountId()).isEqualTo(102L);
        assertThat(result.getDescription()).isEqualTo("Transfer to account 102");

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should get transactions by account ID and return list ordered by timestamp")
    void testGetTransactionsByAccountId_ReturnsTransactions() {
        // Given
        Long accountId = 101L;
        Long userId = 1L;
        String role = "PERSON";
        String pin = "123456";
        
        // Mock account service to return account owned by user
        com.banking.transaction.dto.AccountDto accountDto = new com.banking.transaction.dto.AccountDto();
        accountDto.setId("101");
        accountDto.setUserId(userId);
        when(accountServiceClient.getAccountById(String.valueOf(accountId))).thenReturn(accountDto);
        
        // Mock PIN validation
        when(registerServiceClient.validatePin(userId, pin)).thenReturn(true);
        
        Transaction tx1 = Transaction.builder()
                .id(1001L)
                .accountId(accountId)
                .transactionType("DEPOSIT")
                .amount(new BigDecimal("500.00"))
                .timestamp(now.minusHours(2))
                .description("First deposit")
                .build();

        Transaction tx2 = Transaction.builder()
                .id(1002L)
                .accountId(accountId)
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("100.00"))
                .timestamp(now.minusHours(1))
                .description("Withdrawal")
                .build();

        Transaction tx3 = Transaction.builder()
                .id(1003L)
                .accountId(accountId)
                .transactionType("DEPOSIT")
                .amount(new BigDecimal("200.00"))
                .timestamp(now)
                .description("Second deposit")
                .build();

        List<Transaction> transactions = Arrays.asList(tx3, tx2, tx1); // Ordered by timestamp desc
        when(transactionRepository.findByAccountIdOrderByTimestampDesc(accountId)).thenReturn(transactions);

        // When
        List<TransactionDto> result = transactionService.getTransactionsByAccountId(accountId, pin, userId, role);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1003L);
        assertThat(result.get(0).getType()).isEqualTo("DEPOSIT");
        assertThat(result.get(1).getId()).isEqualTo(1002L);
        assertThat(result.get(1).getType()).isEqualTo("WITHDRAWAL");
        assertThat(result.get(2).getId()).isEqualTo(1001L);
        assertThat(result.get(2).getType()).isEqualTo("DEPOSIT");

        verify(accountServiceClient, times(1)).getAccountById(String.valueOf(accountId));
        verify(registerServiceClient, times(1)).validatePin(userId, pin);
        verify(transactionRepository, times(1)).findByAccountIdOrderByTimestampDesc(accountId);
    }

    @Test
    @DisplayName("Should return empty list when no transactions found for account")
    void testGetTransactionsByAccountId_EmptyList() {
        // Given
        Long accountId = 999L;
        Long userId = 1L;
        String role = "PERSON";
        String pin = "123456";
        
        // Mock account service to return account owned by user
        com.banking.transaction.dto.AccountDto accountDto = new com.banking.transaction.dto.AccountDto();
        accountDto.setId("999");
        accountDto.setUserId(userId);
        when(accountServiceClient.getAccountById(String.valueOf(accountId))).thenReturn(accountDto);
        
        // Mock PIN validation
        when(registerServiceClient.validatePin(userId, pin)).thenReturn(true);
        
        when(transactionRepository.findByAccountIdOrderByTimestampDesc(accountId)).thenReturn(Arrays.asList());

        // When
        List<TransactionDto> result = transactionService.getTransactionsByAccountId(accountId, pin, userId, role);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(accountServiceClient, times(1)).getAccountById(String.valueOf(accountId));
        verify(registerServiceClient, times(1)).validatePin(userId, pin);
        verify(transactionRepository, times(1)).findByAccountIdOrderByTimestampDesc(accountId);
    }

    @Test
    @DisplayName("Should get transaction by ID successfully")
    void testGetTransactionById_Success() {
        // Given
        Long transactionId = 1001L;
        Long userId = 1L;
        String role = "PERSON";
        
        // Mock account service to return account owned by user
        com.banking.transaction.dto.AccountDto accountDto = new com.banking.transaction.dto.AccountDto();
        accountDto.setId("101");
        accountDto.setUserId(userId);
        when(accountServiceClient.getAccountById("101")).thenReturn(accountDto);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // When
        TransactionDto result = transactionService.getTransactionById(transactionId, userId, role);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getAccountId()).isEqualTo(101L);
        assertThat(result.getType()).isEqualTo("DEPOSIT");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getStatus()).isEqualTo("COMPLETED");

        verify(accountServiceClient, times(1)).getAccountById("101");
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("Should throw TransactionNotFoundException when transaction not found")
    void testGetTransactionById_NotFound_ThrowsException() {
        // Given
        Long transactionId = 9999L;
        Long userId = 1L;
        String role = "PERSON";
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId, userId, role))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found with ID: " + transactionId);

        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("Should map Transaction entity to TransactionDto correctly")
    void testMapToDto_Mapping() {
        // Given
        Long userId = 1L;
        String role = "PERSON";
        Transaction txWithRelated = Transaction.builder()
                .id(1005L)
                .accountId(101L)
                .transactionType("TRANSFER")
                .amount(new BigDecimal("750.50"))
                .relatedAccountId(102L)
                .description("Transfer transaction")
                .timestamp(now)
                .build();

        // Mock account service to return account owned by user
        com.banking.transaction.dto.AccountDto accountDto = new com.banking.transaction.dto.AccountDto();
        accountDto.setId("101");
        accountDto.setUserId(userId);
        when(accountServiceClient.getAccountById("101")).thenReturn(accountDto);
        when(transactionRepository.findById(1005L)).thenReturn(Optional.of(txWithRelated));

        // When
        TransactionDto result = transactionService.getTransactionById(1005L, userId, role);

        // Then
        assertThat(result.getId()).isEqualTo(1005L);
        assertThat(result.getAccountId()).isEqualTo(101L);
        assertThat(result.getType()).isEqualTo("TRANSFER");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("750.50"));
        assertThat(result.getRelatedAccountId()).isEqualTo(102L);
        assertThat(result.getDescription()).isEqualTo("Transfer transaction");
        assertThat(result.getTimestamp()).isEqualTo(now);
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
    }
}
