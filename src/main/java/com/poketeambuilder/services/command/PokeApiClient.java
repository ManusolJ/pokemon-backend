package com.poketeambuilder.services.command;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResourceList;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;

import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class PokeApiClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

    private final RestClient restClient;

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_PAGE_SIZE = 100;

    public PokeApiClient(@Qualifier("pokeApiRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public PokeApiResourceList fetchResourceList(String endpoint, int offset, int limit) {
        log.debug("Fetching resource list: {}?offset={}&limit={}", endpoint, offset, limit);

        return restClient.get()
                .uri(endpoint + "?offset={offset}&limit={limit}", offset, limit)
                .retrieve()
                .body(PokeApiResourceList.class);
    }

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


    public <T> T fetchResource(String url, Class<T> responseType) {
        log.debug("Fetching resource: {}", url);

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(responseType);
    }
}