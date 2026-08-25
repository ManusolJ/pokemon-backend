package com.poketeambuilder.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Connection settings for the PokeAPI client, bound from {@code app.pokeapi}.
 *
 * <p>These were compile-time constants on {@link PokeApiConfig}, which meant a test or a mirror
 * could not point the client anywhere else and the throttle could not be changed without a
 * rebuild. The defaults below reproduce the previous hardcoded values, so an environment that
 * sets nothing behaves exactly as before.</p>
 *
 * @param baseUrl       root of the PokeAPI v2 REST surface
 * @param connectTimeout how long to wait for the TCP connection
 * @param readTimeout    how long to wait for a response body
 * @param requestDelay   minimum spacing between consecutive outbound requests
 */
@ConfigurationProperties("app.pokeapi")
public record PokeApiProperties(
        @DefaultValue("https://pokeapi.co/api/v2") String baseUrl,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("10s") Duration readTimeout,
        @DefaultValue("200ms") Duration requestDelay) {
}
