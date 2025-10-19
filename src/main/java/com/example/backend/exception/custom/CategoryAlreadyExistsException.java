package com.example.backend.exception.custom;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String name) {
        super("Category already exists: " + name);
    }
    public CategoryAlreadyExistsException(String name, Throwable cause) {
        super("Category already exists: " + name, cause);
    }
}
