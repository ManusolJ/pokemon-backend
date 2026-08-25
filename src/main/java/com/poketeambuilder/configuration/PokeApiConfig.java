package com.poketeambuilder.configuration;

import com.poketeambuilder.infrastructure.exceptions.PokeApiException;
import com.poketeambuilder.infrastructure.exceptions.PokeApiRateLimitException;
import com.poketeambuilder.infrastructure.interceptors.PokeApiThrottlingInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

import lombok.extern.slf4j.Slf4j;

/**
 * The {@link RestClient} used for PokeAPI ingestion: timeouts, outbound throttling, and the
 * status handlers that turn HTTP failures into the project's own exception types.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class PokeApiConfig {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    @Bean
    RestClient pokeApiRestClient(Builder builder, PokeApiProperties properties) {

        HttpClientSettings clientSettings = HttpClientSettings.defaults()
            .withReadTimeout(properties.readTimeout())
            .withConnectTimeout(properties.connectTimeout());

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.jdk()
            .build(clientSettings);

        return builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(new PokeApiThrottlingInterceptor(properties.requestDelay().toMillis()))
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    int status = response.getStatusCode().value();
                    if (status == HTTP_TOO_MANY_REQUESTS) {
                        String uri = request.getURI().toString();
                        log.warn("PokeAPI rate limit hit on request: {}", uri);
                        throw new PokeApiRateLimitException(String.format("Rate limited by PokeAPI on request: %s", uri));
                    }

                    throw new PokeApiException(status, String.format("PokeAPI client error %d on request: %s", status, request.getURI()));
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    int status = response.getStatusCode().value();
                    throw new PokeApiException(status, String.format("PokeAPI server error %d on request: %s", status, request.getURI()));
                })
                .build();
    }
}
