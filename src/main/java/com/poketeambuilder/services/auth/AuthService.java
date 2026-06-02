package com.poketeambuilder.services.auth;

import java.util.UUID;
import java.time.Instant;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;
import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

import com.poketeambuilder.dtos.auth.LoginDto;
import com.poketeambuilder.dtos.auth.RegisterDto;
import com.poketeambuilder.dtos.auth.TokenResponseDto;
import com.poketeambuilder.dtos.auth.RefreshTokenRequestDto;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.RefreshToken;

import com.poketeambuilder.repositories.UserRepository;

import com.poketeambuilder.mappers.implementation.UserMapper;

import com.poketeambuilder.services.command.AuditLogCommandService;

import com.poketeambuilder.utils.enums.AuditAction;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Registration, login, refresh, and logout. Issues short-lived access JWTs paired with
 * longer-lived refresh tokens. Refresh tokens rotate every use; reuse of an already-revoked
 * token triggers a family-wide revocation and a {@link AuditAction#SECURITY_REFRESH_TOKEN_REUSE_DETECTED}
 * audit entry.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository appUserRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogCommandService auditLogCommandService;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${app.jwt.accessTokenExpirationMs}")
    private int accessTokenExpirationMs;

    @Value("${app.jwt.refreshTokenExpirationMs}")
    private int refreshTokenExpirationMs;

    /** Registers a new user with a fresh refresh-token family. */
    @Transactional
    public TokenResponseDto register(RegisterDto registerDto) {
        if (appUserRepository.existsByUsernameAndDeletedAtIsNull(registerDto.getUsername())) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }
        if (appUserRepository.existsByEmailAndDeletedAtIsNull(registerDto.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }

        AppUser newUser = userMapper.toEntity(registerDto);
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        appUserRepository.save(newUser);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(newUser.getUsername());

        return buildTokenResponse(userDetails, newUser);
    }

    /** Authenticates by username-or-email + password, issues a new token pair under a fresh family. */
    public TokenResponseDto login(LoginDto loginDto) {
        AppUser user = resolveUser(loginDto.getIdentifier());

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), loginDto.getPassword()));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());

        return buildTokenResponse(userDetails, user);
    }

    /** Revokes the entire refresh-token family belonging to the supplied refresh token. */
    @Transactional
    public void logout(RefreshTokenRequestDto refreshTokenDto) {
        RefreshToken storedToken = refreshTokenService.findByRawToken(refreshTokenDto.refreshToken());
        refreshTokenService.revokeFamily(storedToken.getFamilyId());
    }

    /**
     * Rotates the refresh token: marks the supplied one as revoked, issues a new pair under
     * the same family. If the supplied token is already revoked, treats it as a replay attack,
     * revokes the entire family, and writes a {@code SECURITY_REFRESH_TOKEN_REUSE_DETECTED}
     * audit log entry.
     */
    @Transactional
    public TokenResponseDto refresh(RefreshTokenRequestDto refreshTokenDto) {
        String rawToken = refreshTokenDto.refreshToken();

        RefreshToken storedToken = refreshTokenService.findByRawToken(rawToken);

        if (storedToken.isRevoked()) {
            UUID familyId = storedToken.getFamilyId();
            String username = storedToken.getUser().getUsername();
            refreshTokenService.revokeFamily(familyId);
            log.warn("Refresh token reuse detected for user '{}', family {} — revoking all sessions", username, familyId);
            auditLogCommandService.log(username,
                    AuditAction.SECURITY_REFRESH_TOKEN_REUSE_DETECTED,
                    "RefreshToken",
                    familyId.toString(),
                    "Reuse detected; all sessions in family revoked");
            throw new InvalidTokenException("Refresh token reuse detected — all sessions revoked");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }

        refreshTokenService.revoke(storedToken);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(storedToken.getUser().getUsername());

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        refreshTokenService.create(storedToken.getUser(), newRefreshToken, storedToken.getFamilyId(),
                Instant.now().plusMillis(refreshTokenExpirationMs));

        return new TokenResponseDto(newAccessToken, newRefreshToken, accessTokenExpirationMs);
    }

    private AppUser resolveUser(String identifier) {
        if (identifier.contains("@")) {
            return appUserRepository.findByEmailAndDeletedAtIsNull(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + identifier));
        }

        return appUserRepository.findByUsernameAndDeletedAtIsNull(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + identifier));
    }

    private TokenResponseDto buildTokenResponse(UserDetails userDetails, AppUser user) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        UUID familyId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs);

        refreshTokenService.create(user, refreshToken, familyId, expiresAt);

        return new TokenResponseDto(accessToken, refreshToken, accessTokenExpirationMs);
    }
}
