package com.poketeambuilder.infrastructure.scheduling;

import com.poketeambuilder.services.auth.RefreshTokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupTask.class);

    private final RefreshTokenService refreshTokenService;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredTokens() {
        log.info("Starting expired refresh token cleanup");

        refreshTokenService.purgeExpired();

        log.info("Expired refresh token cleanup completed");
    }
}