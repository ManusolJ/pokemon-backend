package com.poketeambuilder.infrastructure.exceptions;

/** Thrown when a user-supplied current password doesn't match the stored hash during password change. Maps to HTTP 400. */
public class BadPasswordException extends RuntimeException {

    public BadPasswordException(String message) {
        super(message);
    }

    public BadPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
