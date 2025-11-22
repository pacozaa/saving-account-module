package com.banking.account.service;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.entity.Account;
import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
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
class AccountServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @InjectMocks
    private AccountService accountService;
    
    private Account testAccount;
    private CreateAccountRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
            .id("1234567")
            .userId(1L)
            .balance(new BigDecimal("1000.00"))
            .accountType("SAVINGS")
            .createdAt(LocalDateTime.now())
            .build();
        
        createRequest = new CreateAccountRequest(1L, "SAVINGS", new BigDecimal("500.00"));
    }
    
    @Test
    void testCreateAccount_Success() {
        // Given
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        // When
        AccountDto result = accountService.createAccount(createRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getAccountType()).isEqualTo("SAVINGS");
        verify(accountRepository).save(any(Account.class));
    }
    
    @Test
    void testCreateAccount_WithNullInitialBalance_DefaultsToZero() {
        // Given
        CreateAccountRequest requestWithoutBalance = new CreateAccountRequest(1L, "CHECKING", null);
        Account accountWithZeroBalance = Account.builder()
            .id("7654321")
            .userId(1L)
            .balance(BigDecimal.ZERO)
            .accountType("CHECKING")
            .createdAt(LocalDateTime.now())
            .build();
        
        when(accountRepository.save(any(Account.class))).thenReturn(accountWithZeroBalance);
        
        // When
        AccountDto result = accountService.createAccount(requestWithoutBalance);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);
        verify(accountRepository).save(any(Account.class));
    }
    
    @Test
    void testUpdateBalance_Success() {
        // Given
        String accountId = "1234567";
        BigDecimal amount = new BigDecimal("500.00");
        BigDecimal expectedBalance = new BigDecimal("1500.00");
        
        Account updatedAccount = Account.builder()
            .id(accountId)
            .userId(1L)
            .balance(expectedBalance)
            .accountType("SAVINGS")
            .createdAt(LocalDateTime.now())
            .build();
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
        
        // When
        AccountDto result = accountService.updateBalance(accountId, amount);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBalance()).isEqualTo(expectedBalance);
        verify(accountRepository).findById(accountId);
        verify(accountRepository).save(any(Account.class));
    }
    
    @Test
    void testUpdateBalance_InsufficientFunds_ThrowsException() {
        // Given
        String accountId = "1234567";
        BigDecimal amount = new BigDecimal("-1500.00"); // More than current balance
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        
        // When & Then
        assertThatThrownBy(() -> accountService.updateBalance(accountId, amount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Insufficient funds");
        
        verify(accountRepository).findById(accountId);
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    void testUpdateBalance_AccountNotFound_ThrowsException() {
        // Given
        String accountId = "9999999";
        BigDecimal amount = new BigDecimal("100.00");
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> accountService.updateBalance(accountId, amount))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("Account not found with id: " + accountId);
        
        verify(accountRepository).findById(accountId);
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    void testGetAccount_NotFound_ThrowsException() {
        // Given
        String accountId = "9999999";
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> accountService.getAccount(accountId))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("Account not found with id: " + accountId);
        
        verify(accountRepository).findById(accountId);
    }
    
    @Test
    void testGetAccount_Success() {
        // Given
        String accountId = "1234567";
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        
        // When
        AccountDto result = accountService.getAccount(accountId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(accountId);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(result.getAccountType()).isEqualTo("SAVINGS");
        verify(accountRepository).findById(accountId);
    }
    
    @Test
    void testGetAccountsByUserId_ReturnsAccounts() {
        // Given
        Long userId = 1L;
        
        Account account1 = Account.builder()
            .id("1234567")
            .userId(userId)
            .balance(new BigDecimal("1000.00"))
            .accountType("SAVINGS")
            .createdAt(LocalDateTime.now())
            .build();
        
        Account account2 = Account.builder()
            .id("7654321")
            .userId(userId)
            .balance(new BigDecimal("2000.00"))
            .accountType("CHECKING")
            .createdAt(LocalDateTime.now())
            .build();
        
        List<Account> accounts = Arrays.asList(account1, account2);
        
        when(accountRepository.findByUserId(userId)).thenReturn(accounts);
        
        // When
        List<AccountDto> result = accountService.getAccountsByUserId(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("1234567");
        assertThat(result.get(0).getAccountType()).isEqualTo("SAVINGS");
        assertThat(result.get(1).getId()).isEqualTo("7654321");
        assertThat(result.get(1).getAccountType()).isEqualTo("CHECKING");
        verify(accountRepository).findByUserId(userId);
    }
    
    @Test
    void testGetAccountsByUserId_ReturnsEmptyList_WhenNoAccounts() {
        // Given
        Long userId = 999L;
        
        when(accountRepository.findByUserId(userId)).thenReturn(Arrays.asList());
        
        // When
        List<AccountDto> result = accountService.getAccountsByUserId(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(accountRepository).findByUserId(userId);
    }
}
