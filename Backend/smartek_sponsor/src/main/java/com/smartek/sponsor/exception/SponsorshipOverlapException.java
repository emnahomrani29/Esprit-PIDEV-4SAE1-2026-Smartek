package com.smartek.sponsor.exception;

public class SponsorshipOverlapException extends RuntimeException {
    public SponsorshipOverlapException(String message) {
        super(message);
    }
    
    public SponsorshipOverlapException(String message, Throwable cause) {
        super(message, cause);
    }
}
