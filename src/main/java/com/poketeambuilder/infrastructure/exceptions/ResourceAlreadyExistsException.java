package com.poketeambuilder.infrastructure.exceptions;

/** Thrown by command services when a uniqueness constraint would be violated (duplicate username, email, like, etc). Maps to HTTP 409. */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
