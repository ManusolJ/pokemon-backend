package com.poketeambuilder.infrastructure.exceptions;

public class PokeApiException extends RuntimeException {

    public PokeApiException(String message) {
        super(message);
    }

    public PokeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}