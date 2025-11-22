package com.banking.deposit.exception;

public class DepositServiceException extends RuntimeException {
    public DepositServiceException(String message) {
        super(message);
    }
    
    public DepositServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
