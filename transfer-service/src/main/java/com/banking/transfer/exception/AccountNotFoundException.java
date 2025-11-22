package com.banking.transfer.exception;

/**
 * Exception thrown when an account is not found
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId);
    }
}
