package com.banking.transfer.exception;

/**
 * Exception thrown when an invalid PIN is provided for a transfer
 */
public class InvalidPinException extends RuntimeException {
    
    public InvalidPinException() {
        super("Invalid PIN provided for transfer authorization");
    }
    
    public InvalidPinException(Long userId) {
        super(String.format("Invalid PIN provided for user %d", userId));
    }
}
