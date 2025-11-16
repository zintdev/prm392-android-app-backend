package com.example.backend.exception.custom;

public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(Integer id) {
        super("Payment with id '" + id + "' not found");
    }
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
