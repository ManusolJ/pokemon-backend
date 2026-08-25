package com.poketeambuilder.services.auth;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Token issuing and parsing. The security-relevant properties here are that a refresh token
 * cannot stand in for an access token, and that a token this service didn't sign is rejected
 * whatever it claims.
 */
class JwtServiceTest {

    private static final String SECRET = "cG9rZXRlYW0tYnVpbGRlci10ZXN0LXNpZ25pbmcta2V5";
    private static final long ACCESS_TTL_MS = 900_000L;
    private static final long REFRESH_TTL_MS = 604_800_000L;

    private JwtService jwtService;

    private final UserDetails user = User.withUsername("ash")
            .password("")
            .authorities("ROLE_USER")
            .build();

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", ACCESS_TTL_MS);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", REFRESH_TTL_MS);
    }

    @Test
    @DisplayName("An access token carries the subject and the user's authorities")
    void accessTokenCarriesSubjectAndAuthorities() {
        String token = jwtService.generateAccessToken(user);
        Object authorities = jwtService.extractClaim(token, claims -> claims.get("authorities"));

        assertThat(jwtService.extractUsername(token)).isEqualTo("ash");
        assertThat(authorities).isEqualTo(List.of("ROLE_USER"));
    }

    @Test
    @DisplayName("A refresh token carries no authorities")
    void refreshTokenOmitsAuthorities() {
        String token = jwtService.generateRefreshToken(user);
        Object authorities = jwtService.extractClaim(token, claims -> claims.get("authorities"));

        assertThat(authorities).isNull();
    }

    @Test
    @DisplayName("The type claim separates the two kinds, so a refresh token can't authenticate")
    void refreshTokenIsNotAnAccessToken() {
        assertThat(jwtService.isAccessToken(jwtService.generateAccessToken(user))).isTrue();
        assertThat(jwtService.isAccessToken(jwtService.generateRefreshToken(user))).isFalse();

        assertThat(jwtService.extractTokenType(jwtService.generateAccessToken(user))).isEqualTo("access");
        assertThat(jwtService.extractTokenType(jwtService.generateRefreshToken(user))).isEqualTo("refresh");
    }

    @Test
    @DisplayName("A token signed with a different key is rejected")
    void rejectsForeignSignature() {
        SecretKey otherKey = Jwts.SIG.HS256.key().build();

        String forged = Jwts.builder()
                .subject("ash")
                .issuer("poketeam-builder")
                .audience().add("poketeam-builder-api").and()
                .claim("type", "access")
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TTL_MS))
                .signWith(otherKey)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(forged)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("A token minted for another issuer or audience is rejected")
    void rejectsForeignIssuerAndAudience() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

        String wrongIssuer = Jwts.builder()
                .subject("ash")
                .issuer("somebody-else")
                .audience().add("poketeam-builder-api").and()
                .claim("type", "access")
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TTL_MS))
                .signWith(key)
                .compact();

        String wrongAudience = Jwts.builder()
                .subject("ash")
                .issuer("poketeam-builder")
                .audience().add("another-api").and()
                .claim("type", "access")
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TTL_MS))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(wrongIssuer)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractUsername(wrongAudience)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("An expired token is rejected")
    void rejectsExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", -1_000L);

        String expired = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.extractUsername(expired)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("A malformed or empty token raises rather than resolving to a username")
    void rejectsMalformedInput() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt"))
                .isInstanceOf(JwtException.class);

        assertThatThrownBy(() -> jwtService.extractUsername(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Refresh tokens outlive access tokens")
    void refreshTokenOutlivesAccessToken() {
        Date accessExpiry = jwtService.extractClaim(jwtService.generateAccessToken(user), c -> c.getExpiration());
        Date refreshExpiry = jwtService.extractClaim(jwtService.generateRefreshToken(user), c -> c.getExpiration());

        assertThat(refreshExpiry).isAfter(accessExpiry);
    }
}
