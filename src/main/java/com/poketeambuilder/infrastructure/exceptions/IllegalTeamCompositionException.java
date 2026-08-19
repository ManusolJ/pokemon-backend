package com.poketeambuilder.infrastructure.exceptions;

/** Thrown when a team slot assigns an ability or move the chosen form cannot legally have. Maps to HTTP 400. */
public class IllegalTeamCompositionException extends RuntimeException {

    public IllegalTeamCompositionException(String message) {
        super(message);
    }

    public IllegalTeamCompositionException(String message, Throwable cause) {
        super(message, cause);
    }
}
