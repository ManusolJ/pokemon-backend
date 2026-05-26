package com.poketeambuilder.infrastructure.exceptions;

import com.poketeambuilder.services.command.PokeApiClient;

/**
 * Thrown when PokeAPI responds with HTTP 429 (rate limited). Treated as retryable by
 * {@link PokeApiClient}; if retries are exhausted it
 * propagates and maps to HTTP 503 Service Unavailable for the caller.
 */
public class PokeApiRateLimitException extends RuntimeException {

    public PokeApiRateLimitException(String message) {
        super(message);
    }

    public PokeApiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
