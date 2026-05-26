package com.poketeambuilder.infrastructure.exceptions;

/** Thrown when a refresh / password-reset token is missing, malformed, expired, revoked, or replayed. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
