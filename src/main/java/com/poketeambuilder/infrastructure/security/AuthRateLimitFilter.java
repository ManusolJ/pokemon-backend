package com.poketeambuilder.infrastructure.security;

import java.io.IOException;

import java.time.Duration;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bucket;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Per-IP rate limit on the abuse-prone unauthenticated endpoints ({@code /api/auth/**} and
 * {@code /api/contact}). Backed by a Caffeine cache of Bucket4j token buckets: 10 requests
 * per minute per IP, evicted after 5 minutes of inactivity. Returns a JSON 429 with a
 * {@code Retry-After} header when the bucket is empty.
 */
@Component
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_MINUTES = 1;
    private static final List<String> RATE_LIMITED_PATH_PREFIXES = List.of("/api/auth/", "/api/contact");

    private final ObjectMapper objectMapper;

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5))
        .maximumSize(10_000)
        .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        Bucket bucket = buckets.get(ip, k -> createBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(WINDOW_MINUTES * 60));

        Map<String, Object> body = Map.of(
            "status", HttpStatus.TOO_MANY_REQUESTS.value(),
            "error", "Too Many Requests",
            "message", "Rate limit exceeded. Please try again later.",
            "path", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return RATE_LIMITED_PATH_PREFIXES.stream().noneMatch(uri::startsWith);
    }

    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(limit -> limit.capacity(MAX_REQUESTS)
                .refillGreedy(MAX_REQUESTS, Duration.ofMinutes(WINDOW_MINUTES)))
            .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
