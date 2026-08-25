package com.poketeambuilder.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

/**
 * Retention window for tombstoned accounts, bound from {@code app.user-cleanup}.
 *
 * @param gracePeriodDays days a tombstoned account is retained before permanent deletion
 */
@Validated
@ConfigurationProperties("app.user-cleanup")
public record UserCleanupProperties(@DefaultValue("30") @Positive int gracePeriodDays) {
}
