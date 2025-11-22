package com.banking.deposit.service;

import com.banking.deposit.client.AccountClient;
import com.banking.deposit.client.TransactionClient;
import com.banking.deposit.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositService {

    private final AccountClient accountClient;
    private final TransactionClient transactionClient;

    /**
     * Orchestrates deposit operation:
     * 1. Validate account exists by fetching it
     * 2. Update account balance (add deposit amount)
     * 3. Log transaction in transaction service
     */
    public DepositResponse processDeposit(DepositRequest request) {
        log.info("Processing deposit for account: {}, amount: {}", 
                request.getAccountId(), request.getAmount());

        // Step 1: Validate account exists
        AccountDto account = accountClient.getAccount(request.getAccountId());
        log.debug("Account found: {}, current balance: {}", 
                account.getId(), account.getBalance());

        // Step 2: Update balance (add deposit amount)
        UpdateBalanceRequest balanceUpdate = new UpdateBalanceRequest(request.getAmount());
        AccountDto updatedAccount = accountClient.updateBalance(request.getAccountId(), balanceUpdate);
        log.info("Balance updated. New balance: {}", updatedAccount.getBalance());

        // Step 3: Log transaction
        String description = request.getDescription() != null 
                ? request.getDescription() 
                : "Deposit" + (request.getTellerId() != null ? " by teller " + request.getTellerId() : "");
        
        LogTransactionRequest transactionRequest = new LogTransactionRequest(
                Long.parseLong(account.getId()), // Convert String account ID to Long for transaction
                "DEPOSIT",
                request.getAmount(),
                null, // No related account for deposits
                description
        );
        
        TransactionDto transaction = transactionClient.logTransaction(transactionRequest);
        log.info("Transaction logged with ID: {}", transaction.getId());

        // Step 4: Build response
        DepositResponse response = new DepositResponse(
                transaction.getId(),
                updatedAccount.getId(),
                request.getAmount(),
                updatedAccount.getBalance(),
                "Deposit successful"
        );

        log.info("Deposit completed successfully for account: {}", request.getAccountId());
        return response;
    }
}
