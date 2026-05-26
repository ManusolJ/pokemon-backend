package com.poketeambuilder.infrastructure.exceptions;

/** Thrown when a requested entity does not exist or is not visible to the caller. Maps to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
