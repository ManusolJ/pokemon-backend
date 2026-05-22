package com.poketeambuilder.services.command;

import java.util.List;
import java.util.ArrayList;

import com.poketeambuilder.configuration.PokeApiConfig;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResourceList;

import com.poketeambuilder.infrastructure.exceptions.PokeApiException;
import com.poketeambuilder.infrastructure.exceptions.PokeApiRateLimitException;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;

/**
 * Thin wrapper around the PokeAPI {@link RestClient}. Handles the offset/limit pagination
 * walk for list endpoints and provides a retry-on-transient-failure helper for single
 * resource lookups. Rate limiting and timeouts are configured on the underlying RestClient
 * bean ({@link PokeApiConfig}).
 */
@Slf4j
@Service
public class PokeApiClient {

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_PAGE_SIZE = 100;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_BACKOFF_MS = 500;
    private static final int FIRST_RETRYABLE_SERVER_STATUS = 500;

    private final RestClient restClient;

    public PokeApiClient(@Qualifier("pokeApiRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /** Fetches one page of resources from {@code endpoint} using PokeAPI's offset/limit. */
    public PokeApiResourceList fetchResourceList(String endpoint, int offset, int limit) {
        log.debug("Fetching resource list: {}?offset={}&limit={}", endpoint, offset, limit);

        return restClient.get()
                .uri(endpoint + "?offset={offset}&limit={limit}", offset, limit)
                .retrieve()
                .body(PokeApiResourceList.class);
    }

    /** Walks the entire paginated listing at {@code endpoint} and returns the flattened list of resources. */
    public List<PokeApiResource> fetchAllResources(String endpoint) {
        List<PokeApiResource> allResources = new ArrayList<>();

        int offset = DEFAULT_OFFSET;
        int limit = DEFAULT_PAGE_SIZE;

        PokeApiResourceList page;

        do {
            page = fetchResourceList(endpoint, offset, limit);

            if (!page.isEmpty()) {
                allResources.addAll(page.results());
            }

            offset += limit;
        } while (page.hasNext());

        log.info("Fetched {} resources from {}", allResources.size(), endpoint);

        return allResources;
    }

    /**
     * Fetches a single resource. Retries up to {@value #MAX_RETRY_ATTEMPTS} times with
     * exponential backoff on transient failures ({@link PokeApiRateLimitException},
     * server-side {@link PokeApiException}, and network-level
     * {@link ResourceAccessException}). Non-retryable client errors propagate immediately.
     *
     * <p>The loop re-throws the caught exception directly on the final attemp.
     */
    public <T> T fetchResource(String url, Class<T> responseType) {
        log.debug("Fetching resource: {}", url);

        long backoff = INITIAL_RETRY_BACKOFF_MS;

        for (int attempt = 1; ; attempt++) {
            try {
                return restClient.get()
                        .uri(url)
                        .retrieve()
                        .body(responseType);
            } catch (PokeApiRateLimitException | ResourceAccessException e) {
                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                log.warn("Transient PokeAPI failure on attempt {}/{} for {}: {}",
                        attempt, MAX_RETRY_ATTEMPTS, url, e.getMessage());
            } catch (PokeApiException e) {
                if (!isRetryable(e) || attempt >= MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                log.warn("Retryable PokeAPI error on attempt {}/{} for {}: {}",
                        attempt, MAX_RETRY_ATTEMPTS, url, e.getMessage());
            }

            sleepBackoff(backoff);
            backoff *= 2;
        }
    }

    /** A {@link PokeApiException} is retryable when it carries a 5xx HTTP status. */
    private static boolean isRetryable(PokeApiException e) {
        return e.getStatus() >= FIRST_RETRYABLE_SERVER_STATUS;
    }

    private static void sleepBackoff(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during PokeAPI retry backoff", ie);
        }
    }
}
