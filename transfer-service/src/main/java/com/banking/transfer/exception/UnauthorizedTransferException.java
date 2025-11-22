package com.banking.transfer.exception;

/**
 * Exception thrown when a user attempts to transfer from an account they don't own
 */
public class UnauthorizedTransferException extends RuntimeException {
    
    public UnauthorizedTransferException(String accountId, Long userId) {
        super(String.format("User %d is not authorized to transfer from account %s", userId, accountId));
    }
}
