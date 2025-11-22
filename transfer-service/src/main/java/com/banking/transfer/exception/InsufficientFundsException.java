package com.banking.transfer.exception;

/**
 * Exception thrown when an account has insufficient funds for a transfer
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountId) {
        super("Insufficient funds in account: " + accountId);
    }
}
