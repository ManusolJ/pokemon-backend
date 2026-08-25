package com.poketeambuilder.controllers;

import java.util.Map;
import java.util.LinkedHashMap;

import org.springframework.context.i18n.LocaleContextHolder;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.validation.FieldError;

import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.poketeambuilder.dtos.error.ErrorResponseDto;

import com.poketeambuilder.infrastructure.exceptions.PokeApiException;
import com.poketeambuilder.infrastructure.exceptions.BadPasswordException;
import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;
import com.poketeambuilder.infrastructure.exceptions.InvalidOperationException;
import com.poketeambuilder.infrastructure.exceptions.PokeApiRateLimitException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;
import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.infrastructure.exceptions.IllegalTeamCompositionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralised mapping from thrown exceptions to {@link ErrorResponseDto} HTTP responses.
 * Every business-domain exception is mapped explicitly so the response shape is predictable;
 * the trailing {@link #handleGeneric(Exception, HttpServletRequest)} catches the unexpected
 * and logs at ERROR making every 500 leave a trail.
 *
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Validation failures on a {@code @Valid @RequestBody}. Overridden rather than declared as a
     * fresh {@code @ExceptionHandler}: the base class already maps this type, and two mappings for
     * one exception in the same advice fails the context at startup.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields are invalid",
                pathOf(request),
                fieldErrors);

        return super.handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Re-bodies everything the base class handles, so framework failures and domain failures look
     * the same to the caller. The message comes from Spring's own {@link ProblemDetail} detail
     * where there is one - that text is already written for client consumption - and falls back
     * to the status reason phrase, so nothing internal leaks.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Framework exception at {}", pathOf(request), ex);
        } else {
            log.debug("Framework exception at {}: {}", pathOf(request), ex.getMessage());
        }

        ErrorResponseDto errorBody = new ErrorResponseDto(
                statusCode.value(),
                reasonPhrase(statusCode),
                detailFor(ex, statusCode),
                pathOf(request));

        return super.handleExceptionInternal(ex, errorBody, headers, statusCode, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username/email or password", request);
    }

    /**
     * Only reachable from the paths that resolve a {@code UserDetails} directly; the
     * authentication manager hides it behind {@link BadCredentialsException}. Answered as a
     * generic 401 so the exception's "User not found: x" message never reaches the caller.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        log.debug("Unresolvable principal at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid or expired credentials", request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponseDto> handleDisabled(DisabledException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "This account has been disabled", request);
    }

    /**
     * Covers denials raised by {@code @PreAuthorize} on a controller method. Denials from the
     * authorization filter never reach this class, so {@code AuthAccessDeniedHandler} answers
     * those with the same status and body.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.debug("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", request);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleAlreadyExists(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadPasswordException.class)
    public ResponseEntity<ErrorResponseDto> handleBadPassword(BadPasswordException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.debug("Constraint violation at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "One or more request parameters are invalid", request);
    }

    @ExceptionHandler(IllegalTeamCompositionException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalTeamComposition(IllegalTeamCompositionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Caught when a request references a non-existent FK (e.g. a malicious pokemonId in a
     * team save). Mapped to 400 with a generic message so constraint names are not leaked.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Request references invalid or conflicting data", request);
    }

    @ExceptionHandler(PokeApiRateLimitException.class)
    public ResponseEntity<ErrorResponseDto> handleRateLimitingFromPokeApi(PokeApiRateLimitException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    @ExceptionHandler(PokeApiException.class)
    public ResponseEntity<ErrorResponseDto> handlePokeApi(PokeApiException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    /**
     * Catch-all for anything not explicitly mapped above. Logs the full stack trace so a
     * production 500 leaves something investigatable; the response stays sanitized so we
     * don't leak internals to the caller.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponseDto body = new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    private String detailFor(Exception ex, HttpStatusCode statusCode) {
        if (ex instanceof ErrorResponse errorResponse) {
            ProblemDetail problemDetail = errorResponse.updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
            String detail = problemDetail == null ? null : problemDetail.getDetail();
            if (detail != null && !detail.isBlank()) {
                return detail;
            }
        }

        return reasonPhrase(statusCode);
    }

    private String reasonPhrase(HttpStatusCode statusCode) {
        HttpStatus resolved = HttpStatus.resolve(statusCode.value());
        return resolved == null ? "Error" : resolved.getReasonPhrase();
    }

    private String pathOf(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }

        return request.getDescription(false);
    }
}
