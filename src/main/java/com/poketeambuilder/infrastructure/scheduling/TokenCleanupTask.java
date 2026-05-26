package com.poketeambuilder.infrastructure.scheduling;

import com.poketeambuilder.services.auth.RefreshTokenService;
import com.poketeambuilder.services.auth.PasswordResetService;

import org.springframework.stereotype.Component;

import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled nightly purge of expired refresh tokens and password-reset tokens.
 * Each service's purge runs in its own try/catch so a failure on one does not
 * cancel the other.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredTokens() {
        log.info("Starting expired token cleanup");

        try {
            refreshTokenService.purgeExpired();
        } catch (Exception e) {
            log.error("Failed to purge expired refresh tokens", e);
        }

        try {
            passwordResetService.purgeExpired();
        } catch (Exception e) {
            log.error("Failed to purge expired password-reset tokens", e);
        }

        log.info("Expired token cleanup completed");
    }
}
