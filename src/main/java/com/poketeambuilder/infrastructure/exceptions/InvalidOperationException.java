package com.poketeambuilder.infrastructure.exceptions;

/** Thrown when a request is well-formed but conflicts with the current state, such as removing the last administrator. Maps to HTTP 409. */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
