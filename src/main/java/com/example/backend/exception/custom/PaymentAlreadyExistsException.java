package com.example.backend.exception.custom;

public class PaymentAlreadyExistsException extends RuntimeException {
    
    public PaymentAlreadyExistsException(String message) {
        super(message);
    }
}
