package com.poketeambuilder.configuration;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Binding for the PokeAPI client settings.
 *
 * <p>These moved out of compile-time constants, so the defaults are now load-bearing: an
 * environment that sets nothing has to end up with exactly the values that used to be hardcoded.
 * Record binding also fails silently into nulls if the component names do not line up, and no
 * other test starts a context that would notice.</p>
 */
class PokeApiPropertiesTest {

    @Test
    @DisplayName("An empty configuration reproduces the previously hardcoded values")
    void defaultsMatchTheOldConstants() {
        PokeApiProperties properties = bind(Map.of());

        assertThat(properties.baseUrl()).isEqualTo("https://pokeapi.co/api/v2");
        assertThat(properties.connectTimeout()).isEqualTo(Duration.ofMillis(5000));
        assertThat(properties.readTimeout()).isEqualTo(Duration.ofMillis(10000));
        assertThat(properties.requestDelay()).isEqualTo(Duration.ofMillis(200));
    }

    @Test
    @DisplayName("Every setting can be overridden, so a mirror or a test double can be pointed at")
    void everySettingIsOverridable() {
        PokeApiProperties properties = bind(Map.of(
                "app.pokeapi.base-url", "http://localhost:9999/api/v2",
                "app.pokeapi.connect-timeout", "1s",
                "app.pokeapi.read-timeout", "2s",
                "app.pokeapi.request-delay", "50ms"));

        assertThat(properties.baseUrl()).isEqualTo("http://localhost:9999/api/v2");
        assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(1));
        assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(properties.requestDelay()).isEqualTo(Duration.ofMillis(50));
    }

    @Test
    @DisplayName("A partial override leaves the remaining defaults intact")
    void partialOverrideKeepsTheRest() {
        PokeApiProperties properties = bind(Map.of("app.pokeapi.request-delay", "1s"));

        assertThat(properties.requestDelay()).isEqualTo(Duration.ofSeconds(1));
        assertThat(properties.baseUrl()).isEqualTo("https://pokeapi.co/api/v2");
    }

    private PokeApiProperties bind(Map<String, Object> properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        return new Binder(source)
                .bindOrCreate("app.pokeapi", PokeApiProperties.class);
    }
}
