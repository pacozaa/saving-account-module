package com.banking.transfer.service;

import com.banking.transfer.client.AccountClient;
import com.banking.transfer.client.TransactionClient;
import com.banking.transfer.dto.*;
import com.banking.transfer.exception.AccountNotFoundException;
import com.banking.transfer.exception.InsufficientFundsException;
import com.banking.transfer.exception.SameAccountTransferException;
import com.banking.transfer.exception.UnauthorizedTransferException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for handling fund transfers between accounts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountClient accountClient;
    private final TransactionClient transactionClient;

    /**
     * Transfer funds from one account to another
     *
     * @param request Transfer request containing source, destination, and amount
     * @param authenticatedUserId The ID of the authenticated user making the request
     * @return Transfer response with transaction details
     */
    public TransferResponse transfer(TransferRequest request, Long authenticatedUserId) {
        String fromAccountId = String.valueOf(request.getFromAccountId());
        String toAccountId = String.valueOf(request.getToAccountId());
        BigDecimal amount = request.getAmount();
        
        log.info("Processing transfer: from={}, to={}, amount={}", 
                fromAccountId, toAccountId, amount);
        
        // 1. Validate not transferring to the same account
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            log.error("Same account transfer attempted: {}", fromAccountId);
            throw new SameAccountTransferException();
        }
        
        // 2. Validate sender account exists and user owns it
        AccountDto senderAccount;
        try {
            senderAccount = accountClient.getAccount(fromAccountId);
            log.info("Sender account found: id={}, userId={}, balance={}", 
                    senderAccount.getId(), senderAccount.getUserId(), senderAccount.getBalance());
        } catch (FeignException.NotFound e) {
            log.error("Sender account not found: {}", fromAccountId);
            throw new AccountNotFoundException(fromAccountId);
        }
        
        // 3. Verify the authenticated user owns the source account
        if (!senderAccount.getUserId().equals(authenticatedUserId)) {
            log.error("User {} attempted to transfer from account {} owned by user {}",
                    authenticatedUserId, fromAccountId, senderAccount.getUserId());
            throw new UnauthorizedTransferException(fromAccountId, authenticatedUserId);
        }
        
        // 4. Validate sufficient funds
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            log.error("Insufficient funds in account {}: balance={}, requested={}", 
                    fromAccountId, senderAccount.getBalance(), amount);
            throw new InsufficientFundsException(fromAccountId);
        }
        
        // 5. Validate receiver account exists
        AccountDto receiverAccount;
        try {
            receiverAccount = accountClient.getAccount(toAccountId);
            log.info("Receiver account found: id={}, balance={}", 
                    receiverAccount.getId(), receiverAccount.getBalance());
        } catch (FeignException.NotFound e) {
            log.error("Receiver account not found: {}", toAccountId);
            throw new AccountNotFoundException(toAccountId);
        }
        
        // 6. Deduct from sender
        UpdateBalanceRequest deductRequest = new UpdateBalanceRequest(amount.negate());
        AccountDto updatedSenderAccount = accountClient.updateBalance(fromAccountId, deductRequest);
        log.info("Deducted {} from account {}, new balance: {}", 
                amount, fromAccountId, updatedSenderAccount.getBalance());
        
        // 7. Add to receiver
        UpdateBalanceRequest addRequest = new UpdateBalanceRequest(amount);
        AccountDto updatedReceiverAccount = accountClient.updateBalance(toAccountId, addRequest);
        log.info("Added {} to account {}, new balance: {}", 
                amount, toAccountId, updatedReceiverAccount.getBalance());
        
        // 8. Log transaction for sender (withdrawal)
        LogTransactionRequest senderTransaction = new LogTransactionRequest(
                request.getFromAccountId(),
                "TRANSFER_OUT",
                amount,
                request.getToAccountId(),
                request.getDescription() != null 
                    ? request.getDescription() 
                    : "Transfer to account " + toAccountId
        );
        TransactionDto senderTxn = transactionClient.logTransaction(senderTransaction);
        log.info("Logged sender transaction: {}", senderTxn.getId());
        
        // 9. Log transaction for receiver (deposit)
        LogTransactionRequest receiverTransaction = new LogTransactionRequest(
                request.getToAccountId(),
                "TRANSFER_IN",
                amount,
                request.getFromAccountId(),
                request.getDescription() != null 
                    ? request.getDescription() 
                    : "Transfer from account " + fromAccountId
        );
        TransactionDto receiverTxn = transactionClient.logTransaction(receiverTransaction);
        log.info("Logged receiver transaction: {}", receiverTxn.getId());
        
        // 10. Build response
        TransferResponse response = new TransferResponse(
                senderTxn.getId(),
                request.getFromAccountId(),
                request.getToAccountId(),
                amount,
                updatedSenderAccount.getBalance(),
                updatedReceiverAccount.getBalance(),
                "Transfer successful"
        );
        
        log.info("Transfer completed successfully: txnId={}", senderTxn.getId());
        return response;
    }
}
