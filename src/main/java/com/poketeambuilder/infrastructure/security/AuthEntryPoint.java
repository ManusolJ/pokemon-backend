package com.poketeambuilder.infrastructure.security;

import java.util.Map;
import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Returns a JSON 401 when an unauthenticated request hits a protected endpoint. Differentiates
 * the response message by exception kind so the front-end can decide whether to redirect to
 * login (no credentials) or attempt a token refresh (expired credentials).
 */
@Component
@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", resolveMessage(authException),
                "path", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String resolveMessage(AuthenticationException ex) {
        if (ex instanceof CredentialsExpiredException) {
            return "Your session has expired. Please refresh or log in again.";
        }
        if (ex instanceof InsufficientAuthenticationException) {
            return "Authentication required to access this resource";
        }
        return "You must be authenticated to access this resource";
    }
}
