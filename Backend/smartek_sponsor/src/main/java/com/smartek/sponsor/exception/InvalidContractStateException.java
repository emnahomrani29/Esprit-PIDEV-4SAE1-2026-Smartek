package com.smartek.sponsor.exception;

public class InvalidContractStateException extends RuntimeException {
    public InvalidContractStateException(String message) {
        super(message);
    }
    
    public InvalidContractStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
