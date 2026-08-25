package com.poketeambuilder.infrastructure.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

/**
 * Returns a JSON 403 when an authenticated caller lacks the required authority. Denials
 * raised inside the filter chain never reach {@code GlobalExceptionHandler}, since that only
 * sees exceptions thrown from the controller layer down, so without this the two paths would
 * answer the same refusal with two different body shapes.
 */
@Component
@RequiredArgsConstructor
public class AuthAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorWriter errorWriter;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        errorWriter.write(request, response, HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
    }
}
