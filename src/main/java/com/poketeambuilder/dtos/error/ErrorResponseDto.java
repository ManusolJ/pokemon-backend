package com.poketeambuilder.dtos.error;

import java.util.Map;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> fieldErrors
) {
    public ErrorResponseDto(int status, String error, String message, String path) {
        this(status, error, message, path, Instant.now(), null);
    }

    public ErrorResponseDto(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(status, error, message, path, Instant.now(), fieldErrors);
    }
}