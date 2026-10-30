package com.poketeambuilder.services.auth;

import java.util.UUID;
import java.util.Optional;
import java.time.Instant;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

import com.poketeambuilder.infrastructure.security.AuthenticatedUser;

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
import com.poketeambuilder.utils.identity.IdentifierNormalizer;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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

    /**
     * Hash an unknown identifier's password is checked against, so a miss costs one bcrypt
     * comparison just like a hit. Built lazily with the configured encoder so its cost factor
     * matches real hashes.
     */
    private volatile String timingDummyHash;

    /** Registers a new user with a fresh refresh-token family. */
    @Transactional
    public TokenResponseDto register(RegisterDto registerDto) {
        if (appUserRepository.existsByUsernameIgnoreCaseAndDeletedAtIsNull(registerDto.getUsername())) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }
        if (appUserRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(registerDto.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }

        AppUser newUser = userMapper.toEntity(registerDto);
        newUser.setEmail(IdentifierNormalizer.email(registerDto.getEmail()));
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        appUserRepository.save(newUser);

        return buildTokenResponse(newUser);
    }

    /**
     * Authenticates by username-or-email + password, issues a new token pair under a fresh
     * family. An unknown identifier and a wrong password both end in
     * {@link BadCredentialsException} after one bcrypt comparison each, so neither the response
     * nor its timing can be used to work out which accounts exist.
     */
    public TokenResponseDto login(LoginDto loginDto) {
        Optional<AppUser> resolved = resolveUser(loginDto.getIdentifier());

        if (resolved.isEmpty()) {
            spendEquivalentPasswordCheck(loginDto.getPassword());
            throw new BadCredentialsException("Invalid credentials");
        }

        AppUser user = resolved.get();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), loginDto.getPassword()));

        return buildTokenResponse(user);
    }

    /** Revokes the entire refresh-token family belonging to the supplied refresh token. */
    @Transactional
    public void logout(RefreshTokenRequestDto refreshTokenDto) {
        RefreshToken storedToken = refreshTokenService.findByRawToken(refreshTokenDto.refreshToken());
        refreshTokenService.revokeFamily(storedToken.getFamilyId());
    }

    /**
     * Rotates the refresh token: spends the supplied one, issues a new pair under the same
     * family. If the supplied token was already spent, treats it as a replay attack, revokes
     * the entire family, and writes a {@code SECURITY_REFRESH_TOKEN_REUSE_DETECTED} audit entry.
     *
     * <p>Spending is a conditional UPDATE, so of two concurrent refreshes of one token exactly
     * one wins; the other takes the reuse path instead of minting a second live branch.</p>
     *
     * <p>The rejections below still commit ({@code noRollbackFor}): each one revokes something
     * before throwing, and a rollback would undo the revocation while the audit log claimed it
     * happened, leaving a stolen token family alive.</p>
     */
    @Transactional(noRollbackFor = {InvalidTokenException.class, DisabledException.class})
    public TokenResponseDto refresh(RefreshTokenRequestDto refreshTokenDto) {
        RefreshToken storedToken = refreshTokenService.findByRawToken(refreshTokenDto.refreshToken());

        UUID familyId = storedToken.getFamilyId();
        AppUser owner = storedToken.getUser();

        if (!refreshTokenService.consume(storedToken)) {
            String username = owner.getUsername();
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

        AuthenticatedUser principal = loadActiveUser(owner.getId(), familyId);

        String newAccessToken = jwtService.generateAccessToken(principal);
        String newRefreshToken = jwtService.generateRefreshToken(principal);

        refreshTokenService.create(owner, newRefreshToken, familyId,
                Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()));

        return new TokenResponseDto(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpirationMs());
    }

    /**
     * Resolves the token's owner and refuses to mint anything for an account that can no longer
     * sign in, ending the whole family on the way out.
     */
    private AuthenticatedUser loadActiveUser(Long userId, UUID familyId) {
        AuthenticatedUser principal;

        try {
            principal = customUserDetailsService.loadUserById(userId);
        } catch (UsernameNotFoundException e) {
            refreshTokenService.revokeFamily(familyId);
            throw new InvalidTokenException("Refresh token no longer resolves to an active account");
        }

        if (!principal.isEnabled()) {
            refreshTokenService.revokeFamily(familyId);
            throw new DisabledException("This account has been disabled");
        }

        return principal;
    }

    private Optional<AppUser> resolveUser(String identifier) {
        if (identifier.contains("@")) {
            return appUserRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(IdentifierNormalizer.email(identifier));
        }

        return appUserRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(identifier);
    }

    private void spendEquivalentPasswordCheck(String rawPassword) {
        String dummyHash = timingDummyHash;
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
            timingDummyHash = dummyHash;
        }
        passwordEncoder.matches(rawPassword, dummyHash);
    }

    private TokenResponseDto buildTokenResponse(AppUser user) {
        AuthenticatedUser principal = AuthenticatedUser.of(user);

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        UUID familyId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs());

        refreshTokenService.create(user, refreshToken, familyId, expiresAt);

        return new TokenResponseDto(accessToken, refreshToken, jwtService.getAccessTokenExpirationMs());
    }
}
