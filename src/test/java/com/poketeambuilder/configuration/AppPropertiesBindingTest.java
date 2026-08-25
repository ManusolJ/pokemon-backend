package com.poketeambuilder.configuration;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Binding for the {@code app.*} configuration records.
 *
 * <p>These replaced scattered {@code @Value} injection. Record binding fails into nulls and zeroes
 * rather than errors when a key does not line up with a component name, and the keys in
 * {@code application.yml} were rewritten to kebab-case as part of the move, so the mapping is
 * worth asserting rather than assuming. Defaults matter too: they are what an environment that
 * sets nothing falls back to.</p>
 */
class AppPropertiesBindingTest {

    @Test
    @DisplayName("JWT settings bind from the kebab-case keys the YAML now uses")
    void jwtBinds() {
        JwtProperties properties = bind("app.jwt", JwtProperties.class, Map.of(
                "app.jwt.secret", "c2VjcmV0",
                "app.jwt.access-token-expiration-ms", "900000",
                "app.jwt.refresh-token-expiration-ms", "604800000"));

        assertThat(properties.secret()).isEqualTo("c2VjcmV0");
        assertThat(properties.accessTokenExpirationMs()).isEqualTo(900_000L);
        assertThat(properties.refreshTokenExpirationMs()).isEqualTo(604_800_000L);
    }

    @Test
    @DisplayName("CORS binds its single origin")
    void corsBinds() {
        CorsProperties properties = bind("app.cors", CorsProperties.class,
                Map.of("app.cors.allowed-origin", "https://pokemon-team-builder.com"));

        assertThat(properties.allowedOrigin()).isEqualTo("https://pokemon-team-builder.com");
    }

    @Test
    @DisplayName("Mail addresses bind, including the hyphenated contact-to key")
    void mailBinds() {
        MailProperties properties = bind("app.mail", MailProperties.class, Map.of(
                "app.mail.from", "noreply@example.test",
                "app.mail.contact-to", "inbox@example.test"));

        assertThat(properties.from()).isEqualTo("noreply@example.test");
        assertThat(properties.contactTo()).isEqualTo("inbox@example.test");
    }

    @Test
    @DisplayName("Password reset binds, and falls back to a thirty-minute token")
    void passwordResetBindsAndDefaults() {
        PasswordResetProperties configured = bind("app.password-reset", PasswordResetProperties.class, Map.of(
                "app.password-reset.token-expiration-minutes", "15",
                "app.password-reset.base-url", "https://app.test/reset"));

        PasswordResetProperties defaulted = bind("app.password-reset", PasswordResetProperties.class,
                Map.of("app.password-reset.base-url", "https://app.test/reset"));

        assertThat(configured.tokenExpirationMinutes()).isEqualTo(15);
        assertThat(configured.baseUrl()).isEqualTo("https://app.test/reset");
        assertThat(defaulted.tokenExpirationMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("The retention window defaults to thirty days when nothing sets it")
    void userCleanupDefaults() {
        assertThat(bind("app.user-cleanup", UserCleanupProperties.class, Map.of()).gracePeriodDays())
                .isEqualTo(30);
        assertThat(bind("app.user-cleanup", UserCleanupProperties.class,
                Map.of("app.user-cleanup.grace-period-days", "7")).gracePeriodDays())
                .isEqualTo(7);
    }

    private <T> T bind(String prefix, Class<T> type, Map<String, Object> properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        return new Binder(source).bindOrCreate(prefix, type);
    }
}
