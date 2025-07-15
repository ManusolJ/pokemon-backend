package com.pkm.services.auth;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Date;
import java.time.Instant;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {

    @Value("${jwt.secret}")
    private final String secret;

    @Value("${jwt.expiration}")
    private final long expirationMs;

    @Value("${jwt.refresh-expiration}")
    private final long refreshExpirationMs;

    private SecretKey secretKey;

    private static final String ISSUER = "pokemonTeamBuilder";

    private static final int START_OF_TOKEN = 7;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username, Map<String, Object> claims){
        Instant now = Instant.now();
        Date issuedDate = Date.from(now);
        Date expirationDate = Date.from(now.plusMillis(expirationMs));
        
        return Jwts.builder()
                .issuer(ISSUER)
                .claims(claims)
                .subject(username)
                .issuedAt(issuedDate)
                .expiration(expirationDate)
                .signWith(secretKey, SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        Date issuedDate = Date.from(now);
        Date expirationDate = Date.from(now.plusMillis(refreshExpirationMs));
        
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(issuedDate)
                .expiration(expirationDate)
                .signWith(secretKey, SIG.HS256)
                .compact();
    }

    public String extractToken(HttpServletRequest request) {   
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(START_OF_TOKEN);
        }
        return null;
    }

    public boolean isTokenValid(String token, UserDetails expectedSubject) {
        final String username = extractUsername(token);
        return (username.equals(expectedSubject.getUsername()) && !isTokenExpired(token));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
