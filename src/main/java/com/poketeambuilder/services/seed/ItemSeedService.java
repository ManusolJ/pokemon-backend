package com.poketeambuilder.services.seed;

import com.poketeambuilder.entities.Item;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.item.ItemApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.item.ItemCategoryApiDto;

import com.poketeambuilder.mappers.implementation.ItemMapper;

import com.poketeambuilder.repositories.ItemRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import com.poketeambuilder.utils.enums.RelevantItemCategory;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemSeedService {
    
    private final static Logger log = LoggerFactory.getLogger(ItemSeedService.class);

    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;

    private final PokeApiClient pokeApiClient;

    private static final String ITEM_CATEGORY_ENDPOINT = "/item-category";

    @Transactional
    public SeedResultDto seed() {
        int errors = 0;

        List<PokeApiResource> relevantResources = new ArrayList<>();

        for (RelevantItemCategory category : RelevantItemCategory.values()) {
            try {
                ItemCategoryApiDto resource = pokeApiClient.fetchResource(ITEM_CATEGORY_ENDPOINT + "/" + category.getApiValue(), ItemCategoryApiDto.class);
                relevantResources.addAll(resource.items());
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch item category resource for category: {}", category.getApiValue(), e);
            }
        }

        List<ItemApiDto> apiDtos = new ArrayList<>();

        for (PokeApiResource resource : relevantResources) {
            try {
                ItemApiDto apiDto = pokeApiClient.fetchResource(resource.url(), ItemApiDto.class);

                apiDtos.add(apiDto);
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch item resource: {}", resource.url(), e);
            }
        }

        List<Item> entities = apiDtos.stream()
            .map(itemMapper::toEntity)
            .toList();
        
        itemRepository.saveAll(entities);

        log.info("Seeded {} items ({} fetch errors)", entities.size(), errors);

        return SeedResultDto.of(entities.size(), errors);
    }

    @Transactional
    public void clearSeedData() {
        itemRepository.deleteAllInBatch();
    }
}
