package com.example.backend.exception.custom;

public class PublisherAlreadyExistsException extends RuntimeException {
    
    public PublisherAlreadyExistsException(String name) {
        super("Publisher with name '" + name + "' already exists");
    }
    
    public PublisherAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
