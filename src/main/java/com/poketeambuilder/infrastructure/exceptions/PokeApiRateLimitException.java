package com.poketeambuilder.infrastructure.exceptions;

public class PokeApiRateLimitException extends RuntimeException {

    public PokeApiRateLimitException(String message) {
        super(message);
    }
}