package com.poketeambuilder.services.auth;

import java.util.UUID;
import java.time.Instant;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.RefreshToken;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;

import com.poketeambuilder.repositories.RefreshTokenRepository;

import com.poketeambuilder.utils.token.TokenHashUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Persistence + lifecycle for {@link RefreshToken} rows. Plain values are never stored —
 * we hash with SHA-256 and look up by the hash. Rotation families let the token-reuse
 * detection invalidate every refresh in a lineage on a single call.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /** Looks up the entity for a plain refresh token value, hashing the input. Throws when no match. */
    public RefreshToken findByRawToken(String rawToken) {
        return refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
    }

    /** Issues a new refresh token row hashed under the given family + expiry. */
    @Transactional
    public RefreshToken create(AppUser user, String rawToken, UUID familyId, Instant expiresAt) {
        RefreshToken token = RefreshToken.builder()
                .tokenHash(TokenHashUtil.sha256(rawToken))
                .user(user)
                .familyId(familyId)
                .revoked(false)
                .expiresAt(expiresAt)
                .build();

        return refreshTokenRepository.save(token);
    }

    /** Marks a single token as revoked. */
    @Transactional
    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    /** Revokes every token in the given rotation family in one bulk UPDATE. */
    @Transactional
    public void revokeFamily(UUID familyId) {
        int revoked = refreshTokenRepository.revokeByFamilyId(familyId);
        log.debug("Revoked {} tokens in family {}", revoked, familyId);
    }

    /** Revokes every refresh token issued to a user in one bulk UPDATE. Used by every "kill all sessions" path. */
    @Transactional
    public void revokeAllForUser(Long userId) {
        int revoked = refreshTokenRepository.revokeByUserId(userId);
        log.debug("Revoked {} tokens for user {}", revoked, userId);
    }

    /** Bulk-deletes expired rows. Scheduled cleanup entry point. */
    @Transactional
    public void purgeExpired() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
