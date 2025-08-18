package com.pkm.services.auth;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class JWTService {

    @Value("${security.jwt.secret}")
    private String secretBase64;

    @Value("${security.jwt.access-expiration-ms:900000}") // 15 minutes by default
    private long accessExpirationMs;

    @Value("${security.jwt.refresh-expiration-ms:1209600000}") // 14 days by default
    private long refreshExpirationMs;

    private SecretKey signingKey;

    private static final int TOKEN_PREFIX_LENGTH = 7;

    private static final String ISSUER = "pokemon-forge-app";

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, accessExpirationMs);
    }

    public String generateRefreshToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, refreshExpirationMs);
    }

    private String generateToken(String subject, Map<String, Object> claims, long expiresInMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiresInMs);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(TOKEN_PREFIX_LENGTH);
        }
        return null;
    }
}
