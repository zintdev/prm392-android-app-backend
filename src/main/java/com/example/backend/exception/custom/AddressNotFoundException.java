package com.example.backend.exception.custom;

public class AddressNotFoundException extends RuntimeException {
    
    public AddressNotFoundException(Integer id) {
        super("Address with id '" + id + "' not found");
    }
    
    public AddressNotFoundException(String message) {
        super(message);
    }
}
