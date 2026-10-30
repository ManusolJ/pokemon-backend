package com.poketeambuilder.services.auth;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.poketeambuilder.configuration.JwtProperties;

import com.poketeambuilder.infrastructure.security.AuthenticatedUser;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Token issuing and parsing. The security-relevant properties here are that a refresh token
 * cannot stand in for an access token, that a token this service didn't sign is rejected
 * whatever it claims, that the subject is the immutable user id rather than a reusable name,
 * and that no two tokens are ever identical.
 */
class JwtServiceTest {

    private static final String SECRET = "cG9rZXRlYW0tYnVpbGRlci10ZXN0LXNpZ25pbmcta2V5";
    private static final long ACCESS_TTL_MS = 900_000L;
    private static final long REFRESH_TTL_MS = 604_800_000L;

    private JwtService jwtService;

    private static final long USER_ID = 42L;

    private final AuthenticatedUser user = new AuthenticatedUser(
            USER_ID, "ash", "", true, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(SECRET, ACCESS_TTL_MS, REFRESH_TTL_MS));
    }

    @Test
    @DisplayName("An access token's subject is the user id; the username rides in its own claim")
    void accessTokenCarriesIdSubjectUsernameAndAuthorities() {
        String token = jwtService.generateAccessToken(user);
        Object authorities = jwtService.extractClaim(token, claims -> claims.get("authorities"));

        assertThat(jwtService.extractUserId(token)).isEqualTo(USER_ID);
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        String username = jwtService.extractClaim(token, claims -> claims.get("username", String.class));

        assertThat(subject).isEqualTo("42");
        assertThat(username).isEqualTo("ash");
        assertThat(authorities).isEqualTo(List.of("ROLE_USER"));
    }

    @Test
    @DisplayName("Parsing an access token rebuilds the principal it was issued for")
    void parseAccessTokenRebuildsThePrincipal() {
        Optional<AuthenticatedUser> parsed = jwtService.parseAccessToken(jwtService.generateAccessToken(user));

        assertThat(parsed).isPresent();
        assertThat(parsed.get().getId()).isEqualTo(USER_ID);
        assertThat(parsed.get().getUsername()).isEqualTo("ash");
        assertThat(parsed.get().getAuthorities()).extracting(Object::toString).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("A refresh token parses to no principal, so it can't authenticate a request")
    void parseAccessTokenRefusesRefreshTokens() {
        assertThat(jwtService.parseAccessToken(jwtService.generateRefreshToken(user))).isEmpty();
    }

    @Test
    @DisplayName("A pre-migration token with a username subject parses to no principal instead of failing")
    void parseAccessTokenRefusesLegacyUsernameSubject() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

        String legacy = Jwts.builder()
                .subject("ash")
                .issuer("poketeam-builder")
                .audience().add("poketeam-builder-api").and()
                .claim("type", "access")
                .claim("authorities", List.of("ROLE_USER"))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TTL_MS))
                .signWith(key)
                .compact();

        assertThat(jwtService.parseAccessToken(legacy)).isEmpty();
    }

    @Test
    @DisplayName("Two tokens issued to the same user in the same instant still differ")
    void tokensIssuedTogetherAreDistinct() {
        String first = jwtService.generateRefreshToken(user);
        String second = jwtService.generateRefreshToken(user);

        String firstJti = jwtService.extractClaim(first, Claims::getId);
        String secondJti = jwtService.extractClaim(second, Claims::getId);

        assertThat(first).isNotEqualTo(second);
        assertThat(firstJti).isNotBlank().isNotEqualTo(secondJti);
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

        assertThatThrownBy(() -> jwtService.extractUserId(forged)).isInstanceOf(JwtException.class);
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

        assertThatThrownBy(() -> jwtService.extractUserId(wrongIssuer)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractUserId(wrongAudience)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("An expired token is rejected")
    void rejectsExpiredToken() {
        JwtService alreadyExpired = new JwtService(new JwtProperties(SECRET, -1_000L, REFRESH_TTL_MS));

        String expired = alreadyExpired.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.extractUserId(expired)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("A malformed or empty token raises rather than resolving to a user")
    void rejectsMalformedInput() {
        assertThatThrownBy(() -> jwtService.extractUserId("not-a-jwt"))
                .isInstanceOf(JwtException.class);

        assertThatThrownBy(() -> jwtService.extractUserId(""))
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
