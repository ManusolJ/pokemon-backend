package com.poketeambuilder.services.auth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.RefreshToken;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;

import com.poketeambuilder.repositories.RefreshTokenRepository;

import com.poketeambuilder.utils.token.TokenHashUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRawToken(String rawToken) {
        return refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
    }

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

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void revokeFamily(UUID familyId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByFamilyId(familyId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    public void revokeAllForUser(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    @Transactional
    public void purgeExpired() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
