package com.poketeambuilder.services.auth;

import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and parses HMAC-SHA signed JWTs. Two token kinds share the format — access tokens
 * (short-lived, carry the user's authorities) and refresh tokens (longer-lived, no
 * authorities). The {@code type} claim discriminates between them so the filter chain can
 * refuse a refresh token where an access token is required.
 */
@Service
public class JwtService {

    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private static final String ISSUER = "poketeam-builder";
    private static final String AUDIENCE = "poketeam-builder-api";

    private final SecretKey signingKey;

    @Value("${app.jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /** Builds an access token carrying the user's authorities for the security context. */
    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList();
        return buildToken(Map.of("authorities", roles), userDetails, accessTokenExpirationMs, TOKEN_TYPE_ACCESS);
    }

    /** Builds a refresh token (no authorities embedded). */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(Map.of(), userDetails, refreshTokenExpirationMs, TOKEN_TYPE_REFRESH);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationMs, String tokenType) {
        return Jwts.builder()
                .claims(extraClaims)
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(new Date())
                .signWith(signingKey)
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .compact();
    }

    /** True if the token's signature, subject, and expiration all check out for the supplied user. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** Subject claim. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
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

    /** {@code true} iff the token's {@code type} claim is {@code refresh}. */
    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(extractTokenType(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
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
}
