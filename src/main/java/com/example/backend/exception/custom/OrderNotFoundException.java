package com.example.backend.exception.custom;

public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(Integer id) {
        super("Order with id '" + id + "' not found");
    }
    
    public OrderNotFoundException(String message) {
        super(message);
    }
}
