package com.poketeambuilder.infrastructure.scheduling;

import java.time.Duration;
import java.time.Instant;

import com.poketeambuilder.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled hard-delete of tombstoned users. Runs on the first day of every month at 03:00
 * (server time) and removes every row whose {@code deleted_at} is older than the configured
 * grace window, so a self-deleted account can still be restored by an admin during that
 * window before the row vanishes for good.
 *
 * <p>The grace window defaults to 30 days and is overridable via the
 * {@code app.user-cleanup.grace-period-days} property.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupTask {

    private static final int DEFAULT_GRACE_PERIOD_DAYS = 30;

    private final UserRepository userRepository;

    @Value("${app.user-cleanup.grace-period-days:" + DEFAULT_GRACE_PERIOD_DAYS + "}")
    private int gracePeriodDays;

    @Scheduled(cron = "0 0 3 1 * *")
    @Transactional
    public void purgeTombstonedUsers() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(gracePeriodDays));
        log.info("Purging tombstoned users with deleted_at before {} (grace = {} days)", cutoff, gracePeriodDays);

        try {
            int removed = userRepository.purgeDeletedBefore(cutoff);
            log.info("Tombstoned user cleanup completed. Removed {} row(s)", removed);
        } catch (Exception e) {
            log.error("Failed to purge tombstoned users", e);
        }
    }
}
