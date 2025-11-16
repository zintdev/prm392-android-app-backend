package com.example.backend.exception.custom;

public class PublisherNotFoundException extends RuntimeException {
    
    public PublisherNotFoundException(Integer id) {
        super("Publisher with ID " + id + " not found");
    }
    
    public PublisherNotFoundException(String message) {
        super(message);
    }
}
