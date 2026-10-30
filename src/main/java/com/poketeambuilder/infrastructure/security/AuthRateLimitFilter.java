package com.poketeambuilder.infrastructure.security;

import java.io.IOException;

import java.time.Duration;

import java.util.List;

import com.poketeambuilder.configuration.RateLimitProperties;

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

import lombok.RequiredArgsConstructor;

/**
 * Per-IP rate limit on the abuse-prone unauthenticated endpoints ({@code /api/auth/**} and
 * {@code /api/contact}). Backed by a Caffeine cache of Bucket4j token buckets: 10 requests
 * per minute per IP, evicted after 5 minutes of inactivity. Returns a JSON 429 with a
 * {@code Retry-After} header when the bucket is empty.
 *
 * <p>The bucket key must be an address the client can't choose. {@code X-Forwarded-For} is not
 * one: it is client-supplied, Cloudflare appends to it rather than replacing it, and the
 * {@code framework} forward-headers strategy resolves {@code getRemoteAddr()} from its leftmost
 * entry - so keying on either lets a caller mint a fresh bucket per request. Behind the tunnel
 * the key is {@code CF-Connecting-IP} instead ({@link RateLimitProperties#clientIpHeader()}),
 * which Cloudflare always overwrites. With no header configured the socket address is used.</p>
 */
@Component
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_MINUTES = 1;
    private static final List<String> RATE_LIMITED_PATH_PREFIXES = List.of("/api/auth/", "/api/contact");

    private final SecurityErrorWriter errorWriter;
    private final RateLimitProperties rateLimitProperties;

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5))
        .maximumSize(10_000)
        .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Bucket bucket = buckets.get(clientKey(request), k -> createBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader("Retry-After", String.valueOf(WINDOW_MINUTES * 60));

        errorWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS,
            "Rate limit exceeded. Please try again later.");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return RATE_LIMITED_PATH_PREFIXES.stream().noneMatch(uri::startsWith);
    }

    /** The trusted proxy's client-address header when one is configured and present, else the socket address. */
    private String clientKey(HttpServletRequest request) {
        String header = rateLimitProperties.clientIpHeader();

        if (header != null && !header.isBlank()) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return request.getRemoteAddr();
    }

    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(limit -> limit.capacity(MAX_REQUESTS)
                .refillGreedy(MAX_REQUESTS, Duration.ofMinutes(WINDOW_MINUTES)))
            .build();
    }
}
