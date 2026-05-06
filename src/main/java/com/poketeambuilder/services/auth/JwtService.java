package com.poketeambuilder.services.auth;

import java.util.Map;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000;

    private final static String CLAIM_TOKEN_TYPE = "type";
    private final static String TOKEN_TYPE_ACCESS = "access";
    private final static String TOKEN_TYPE_REFRESH = "refresh";

    private final static String ISSUER = "poketeam-builder";
    private final static String AUDIENCE = "poketeam-builder-api";
    
    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(Map.of(), userDetails, ACCESS_TOKEN_EXPIRATION_MS, TOKEN_TYPE_ACCESS);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(Map.of(), userDetails, REFRESH_TOKEN_EXPIRATION_MS, TOKEN_TYPE_REFRESH);
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

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

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

    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(extractTokenType(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}