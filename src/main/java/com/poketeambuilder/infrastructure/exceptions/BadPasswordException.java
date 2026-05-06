package com.poketeambuilder.infrastructure.exceptions;

public class BadPasswordException extends RuntimeException {

    public BadPasswordException(String message) {
        super(message);
    }
}