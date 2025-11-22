package com.banking.account.service;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.entity.Account;
import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        log.info("Creating account for userId: {}, accountType: {}", request.getUserId(), request.getAccountType());
        
        BigDecimal initialBalance = request.getInitialBalance() != null ? 
            request.getInitialBalance() : BigDecimal.ZERO;
        
        Account account = Account.builder()
            .userId(request.getUserId())
            .accountType(request.getAccountType())
            .balance(initialBalance)
            .build();
        
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with id: {}", savedAccount.getId());
        
        return mapToDto(savedAccount);
    }
    
    @Transactional(readOnly = true)
    public AccountDto getAccount(String accountId) {
        log.info("Fetching account with id: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        
        return mapToDto(account);
    }
    
    @Transactional(readOnly = true)
    public AccountDto getAccount(String accountId, Long authenticatedUserId) {
        log.info("Fetching account with id: {} for user: {}", accountId, authenticatedUserId);
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        
        // Check if the authenticated user owns the account
        if (authenticatedUserId != null && !account.getUserId().equals(authenticatedUserId)) {
            log.warn("User {} attempted to access account {} owned by user {}", 
                    authenticatedUserId, accountId, account.getUserId());
            throw new com.banking.account.exception.UnauthorizedAccessException(
                "You are not authorized to access this account");
        }
        
        return mapToDto(account);
    }
    
    @Transactional
    public AccountDto updateBalance(String accountId, BigDecimal amount) {
        log.info("Updating balance for account: {} by amount: {}", accountId, amount);
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        
        BigDecimal newBalance = account.getBalance().add(amount);
        
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Current balance: " + account.getBalance());
        }
        
        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);
        
        log.info("Balance updated successfully. New balance: {}", newBalance);
        
        return mapToDto(updatedAccount);
    }
    
    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByUserId(Long userId) {
        log.info("Fetching accounts for userId: {}", userId);
        
        List<Account> accounts = accountRepository.findByUserId(userId);
        
        return accounts.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByUserId(Long userId, Long authenticatedUserId) {
        log.info("Fetching accounts for userId: {} by authenticatedUser: {}", userId, authenticatedUserId);
        
        // Check if the authenticated user is requesting their own accounts
        if (authenticatedUserId != null && !userId.equals(authenticatedUserId)) {
            log.warn("User {} attempted to access accounts of user {}", authenticatedUserId, userId);
            throw new com.banking.account.exception.UnauthorizedAccessException(
                "You are not authorized to access accounts of other users");
        }
        
        List<Account> accounts = accountRepository.findByUserId(userId);
        
        return accounts.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
    
    private AccountDto mapToDto(Account account) {
        return AccountDto.builder()
            .id(account.getId())
            .userId(account.getUserId())
            .balance(account.getBalance())
            .accountType(account.getAccountType())
            .createdAt(account.getCreatedAt())
            .build();
    }
}
