package com.poketeambuilder.configuration;

import com.poketeambuilder.infrastructure.exceptions.PokeApiException;
import com.poketeambuilder.infrastructure.exceptions.PokeApiRateLimitException;
import com.poketeambuilder.infrastructure.interceptors.PokeApiThrottlingInterceptor;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;


@Configuration
public class PokeApiConfig {

    private static final Logger log = LoggerFactory.getLogger(PokeApiConfig.class);

    private static final int READ_TIMEOUT_MS = 10000;
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int DEFAULT_REQUEST_DELAY_MS = 200;
    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2";

    @Bean
    RestClient pokeApiRestClient(Builder builder) {

        HttpClientSettings clientSettings = HttpClientSettings.defaults()
            .withReadTimeout(Duration.ofMillis(READ_TIMEOUT_MS))
            .withConnectTimeout(Duration.ofMillis(CONNECTION_TIMEOUT_MS));
 
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.jdk()
            .build(clientSettings);

        return builder
                .baseUrl(POKEAPI_BASE_URL)
                .requestFactory(requestFactory)
                .requestInterceptor(new PokeApiThrottlingInterceptor(DEFAULT_REQUEST_DELAY_MS))
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode().value() == 429) {
                        String uri = request.getURI().toString();
                        log.warn("PokeAPI rate limit hit on request: {}", uri);
                        throw new PokeApiRateLimitException(String.format("Rate limited by PokeAPI on request: %s", uri));
                    }

                    throw new PokeApiException(String.format("PokeAPI client error %d on request: %s", response.getStatusCode().value(), request.getURI()));
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new PokeApiException(String.format("PokeAPI server error %d on request: %s", response.getStatusCode().value(), request.getURI()));
                })
                .build();
    }
}