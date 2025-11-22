package com.banking.transfer.exception;

/**
 * Exception thrown when attempting to transfer to the same account
 */
public class SameAccountTransferException extends RuntimeException {
    public SameAccountTransferException() {
        super("Cannot transfer to the same account");
    }
}
