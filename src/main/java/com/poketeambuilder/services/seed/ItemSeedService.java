package com.poketeambuilder.services.seed;

import java.util.List;
import java.util.ArrayList;

import com.poketeambuilder.entities.Item;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.item.ItemApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.item.ItemCategoryApiDto;

import com.poketeambuilder.mappers.implementation.ItemMapper;

import com.poketeambuilder.repositories.ItemRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import com.poketeambuilder.utils.enums.RelevantItemCategory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the {@code item} table from PokeAPI. Only ingests categories listed in
 * {@link RelevantItemCategory}.
 */
@Slf4j
@Service
public class ItemSeedService {

    private static final String ITEM_CATEGORY_ENDPOINT = "/item-category";

    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;

    private final PokeApiClient pokeApiClient;
    private final TransactionTemplate transactionTemplate;

    public ItemSeedService(ItemMapper itemMapper, ItemRepository itemRepository, PokeApiClient pokeApiClient, TransactionTemplate transactionTemplate) {
        this.itemMapper = itemMapper;
        this.pokeApiClient = pokeApiClient;
        this.itemRepository = itemRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public SeedResultDto seed() {
        FetchResult fetched = fetchAll();
        int saved = transactionTemplate.execute(status -> persist(fetched.apiDtos()));
        log.info("Seeded {} items ({} fetch errors)", saved, fetched.errors());
        return SeedResultDto.of(saved, fetched.errors());
    }

    @Transactional
    public void clearSeedData() {
        itemRepository.deleteAllInBatch();
    }

    private FetchResult fetchAll() {
        List<PokeApiResource> relevantResources = new ArrayList<>();
        int errors = 0;

        for (RelevantItemCategory category : RelevantItemCategory.values()) {
            try {
                ItemCategoryApiDto resource = pokeApiClient.fetchResource(
                        ITEM_CATEGORY_ENDPOINT + "/" + category.getValue(),
                        ItemCategoryApiDto.class);
                relevantResources.addAll(resource.items());
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch item category resource for category: {}", category.getValue(), e);
            }
        }

        List<ItemApiDto> apiDtos = new ArrayList<>();

        for (PokeApiResource resource : relevantResources) {
            try {
                apiDtos.add(pokeApiClient.fetchResource(resource.url(), ItemApiDto.class));
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch item resource: {}", resource.url(), e);
            }
        }

        return new FetchResult(apiDtos, errors);
    }

    private int persist(List<ItemApiDto> apiDtos) {
        List<Item> entities = apiDtos.stream().map(itemMapper::toEntity).toList();
        itemRepository.saveAll(entities);
        return entities.size();
    }

    private record FetchResult(List<ItemApiDto> apiDtos, int errors) {}
}
