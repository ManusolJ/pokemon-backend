package com.poketeambuilder.integration;

import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.context.annotation.Import;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for tests that need the real schema.
 *
 * <p>These run against PostgreSQL rather than an embedded engine because the schema cannot be
 * expressed anywhere else: V23 and V24 both create partial unique indexes, and V24's is on an
 * expression. Flyway builds the schema exactly as production does, so a migration
 * that would fail on deploy fails here first.</p>
 *
 * <p>The database comes from Testcontainers by default. Passing {@code -Dtest.datasource.url}
 * points them at an already-running Postgres instead.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(com.poketeambuilder.configuration.CacheConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIf("com.poketeambuilder.integration.PostgresTestBase#databaseAvailable")
public abstract class PostgresTestBase {

    private static final String POSTGRES_IMAGE = "postgres:17-alpine";

    private static final String EXTERNAL_URL = property("test.datasource.url");
    private static final String EXTERNAL_USER = propertyOrDefault("test.datasource.username", "poketeam");
    private static final String EXTERNAL_PASSWORD = propertyOrDefault("test.datasource.password", "poketeam");

    private static PostgreSQLContainer<?> container;

    static boolean databaseAvailable() {
        return EXTERNAL_URL != null || DockerClientFactory.instance().isDockerAvailable();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        if (EXTERNAL_URL != null) {
            registry.add("spring.datasource.url", () -> EXTERNAL_URL);
            registry.add("spring.datasource.username", () -> EXTERNAL_USER);
            registry.add("spring.datasource.password", () -> EXTERNAL_PASSWORD);
            return;
        }

        registry.add("spring.datasource.url", startedContainer()::getJdbcUrl);
        registry.add("spring.datasource.username", startedContainer()::getUsername);
        registry.add("spring.datasource.password", startedContainer()::getPassword);
    }

    private static synchronized PostgreSQLContainer<?> startedContainer() {
        if (container == null) {
            container = new PostgreSQLContainer<>(POSTGRES_IMAGE);
            container.start();
        }
        return container;
    }

    private static String property(String key) {
        String value = System.getProperty(key);
        return value == null || value.isBlank() ? null : value;
    }

    private static String propertyOrDefault(String key, String fallback) {
        String value = property(key);
        return value == null ? fallback : value;
    }
}
