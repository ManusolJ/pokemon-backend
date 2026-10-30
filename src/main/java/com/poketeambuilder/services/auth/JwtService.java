package com.poketeambuilder.services.auth;

import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.poketeambuilder.configuration.JwtProperties;

import com.poketeambuilder.infrastructure.security.AuthenticatedUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and parses HMAC-SHA signed JWTs. Two token kinds share the format — access tokens
 * (short-lived, carry the user's authorities) and refresh tokens (longer-lived, no
 * authorities). The {@code type} claim discriminates between them so the filter chain can
 * refuse a refresh token where an access token is required.
 *
 * <p>The subject is the user's id, never the username: usernames can be changed and re-registered,
 * so a name-based subject could resolve to a different account than the one the token was issued
 * to. The username rides along in its own claim for display and audit labels only.</p>
 *
 * <p>Every token carries a random {@code jti}. Without it, two tokens issued to the same user in
 * the same second are byte-identical (HMAC is deterministic and the dates are second-precision),
 * and the second refresh token would collide on {@code refresh_token.token_hash UNIQUE}.</p>
 */
@Service
public class JwtService {

    public static final String CLAIM_AUTHORITIES = "authorities";
    public static final String CLAIM_USERNAME = "username";

    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private static final String ISSUER = "poketeam-builder";
    private static final String AUDIENCE = "poketeam-builder-api";

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.accessTokenExpirationMs = properties.accessTokenExpirationMs();
        this.refreshTokenExpirationMs = properties.refreshTokenExpirationMs();
    }

    /**
     * Access-token lifetime, in milliseconds. Exposed so callers that report it to the client can
     * read it from the component that issues the tokens rather than binding the same property a
     * second time and risking the two drifting apart.
     */
    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    /** Refresh-token lifetime, in milliseconds. See {@link #getAccessTokenExpirationMs()}. */
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    /** Builds an access token carrying the user's authorities for the security context. */
    public String generateAccessToken(AuthenticatedUser user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return buildToken(Map.of(CLAIM_AUTHORITIES, roles), user, accessTokenExpirationMs, TOKEN_TYPE_ACCESS);
    }

    /** Builds a refresh token (no authorities embedded). */
    public String generateRefreshToken(AuthenticatedUser user) {
        return buildToken(Map.of(), user, refreshTokenExpirationMs, TOKEN_TYPE_REFRESH);
    }

    private String buildToken(Map<String, Object> extraClaims, AuthenticatedUser user, long expirationMs, String tokenType) {
        return Jwts.builder()
                .claims(extraClaims)
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .claim(CLAIM_USERNAME, user.getUsername())
                .issuedAt(new Date())
                .signWith(signingKey)
                .subject(String.valueOf(user.getId()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .compact();
    }

    /**
     * Verifies an access token and rebuilds the principal it was issued for, parsing it once.
     * Empty when the token is genuine but is not an access token, or carries no usable subject
     * (tokens minted before the subject became the user id hold a username there).
     *
     * @throws io.jsonwebtoken.JwtException on a bad signature, expiry, issuer or audience
     * @throws IllegalArgumentException     on an empty token
     */
    public Optional<AuthenticatedUser> parseAccessToken(String token) {
        Claims claims = extractAllClaims(token);

        if (!TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))) {
            return Optional.empty();
        }

        Long userId = parseUserId(claims.getSubject());
        String username = claims.get(CLAIM_USERNAME, String.class);

        if (userId == null || username == null || username.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new AuthenticatedUser(userId, username, "", true, authoritiesOf(claims)));
    }

    /** Subject claim as a user id, or {@code null} when it isn't one. */
    public Long extractUserId(String token) {
        return parseUserId(extractClaim(token, Claims::getSubject));
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    /** {@code true} if the token's {@code type} claim is {@code access}. */
    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static Long parseUserId(String subject) {
        if (subject == null || subject.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Reads the {@code authorities} claim as a list of role strings and wraps them as {@link SimpleGrantedAuthority}. */
    private static List<GrantedAuthority> authoritiesOf(Claims claims) {
        Object raw = claims.get(CLAIM_AUTHORITIES);
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
