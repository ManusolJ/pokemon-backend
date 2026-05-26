package com.poketeambuilder.infrastructure.security;

import java.util.List;
import java.util.Collection;
import java.io.IOException;

import com.poketeambuilder.services.auth.JwtService;

import org.springframework.stereotype.Component;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bearer-token authentication filter. Reads the {@code Authorization} header, validates the
 * signature and expiry, and installs an {@link UsernamePasswordAuthenticationToken} on the
 * {@link SecurityContextHolder}.
 *
 * <p>Authentication is built directly from JWT claims (subject = username, {@code authorities}
 * claim = role list) — no database round-trip per request. The trade-off is that role and
 * disabled-flag changes only take effect at the next access-token issuance (~15 minutes).
 * Long-lived sessions are handled via the refresh-token rotation flow, which IS DB-backed
 * and revoked immediately on password change / disable.</p>
 *
 * <p>JJWT parsing exceptions (expired, malformed, tampered, bad signature) are caught and
 * logged; the request continues unauthenticated so {@link AuthEntryPoint} can produce a clean
 * 401 instead of the framework leaking a 500 with a JJWT stack trace.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final int AUTH_HEADER_PREFIX_LENGTH = 7;
    private static final String AUTH_HEADER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String AUTHORITIES_CLAIM = "authorities";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTH_HEADER_NAME);

        if (authHeader == null || !authHeader.startsWith(AUTH_HEADER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(AUTH_HEADER_PREFIX_LENGTH);

        try {
            authenticateFromToken(token, request);
        } catch (JwtException e) {
            log.debug("Rejecting JWT for request {}: {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parses the JWT, verifies it is an access token, and populates the security context with
     * a {@link UserDetails} principal so downstream {@code @AuthenticationPrincipal UserDetails}
     * controller params keep working. Throws {@link JwtException} on any parse / signature /
     * expiry failure, the outer {@code doFilterInternal} catches and falls back to anonymous.
     */
    private void authenticateFromToken(String token, HttpServletRequest request) {
        if (!jwtService.isAccessToken(token)) {
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String username = jwtService.extractUsername(token);
        if (username == null || username.isBlank()) {
            return;
        }

        Collection<GrantedAuthority> authorities = extractAuthorities(token);

        UserDetails principal = User.withUsername(username)
                .password("")
                .authorities(authorities)
                .build();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /** Reads the {@code authorities} claim as a list of role strings and wraps them as {@link SimpleGrantedAuthority}. */
    private Collection<GrantedAuthority> extractAuthorities(String token) {
        List<String> roles = jwtService.extractClaim(token, claims -> {
            Object raw = claims.get(AUTHORITIES_CLAIM);
            if (raw instanceof List<?> list) {
                return list.stream().map(Object::toString).toList();
            }
            return List.<String>of();
        });

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
