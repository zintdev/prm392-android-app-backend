package com.example.backend.exception.custom;

public class ArtistAlreadyExistsException extends RuntimeException {

    public ArtistAlreadyExistsException(String name) {
        super("Artist with name '" + name + "' already exists");
    }

    public ArtistAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
