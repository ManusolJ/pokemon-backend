package com.poketeambuilder.infrastructure.exceptions;

import lombok.Getter;

/**
 * Generic wrapper for a non-OK PokeAPI response. Carries the upstream HTTP {@link #status}
 * so callers can decide whether to retry without string-matching the exception message.
 */
@Getter
public class PokeApiException extends RuntimeException {

    private final int status;

    public PokeApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public PokeApiException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
