package com.poketeambuilder.services.command;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.services.seed.TypeSeedService;
import com.poketeambuilder.services.seed.ItemSeedService;
import com.poketeambuilder.services.seed.MoveSeedService;
import com.poketeambuilder.services.seed.NatureSeedService;
import com.poketeambuilder.services.seed.AbilitySeedService;
import com.poketeambuilder.services.seed.PokemonSeedService;
import com.poketeambuilder.services.seed.SpeciesSeedService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedOrchestratorCommandService {

    private final static Logger log = LoggerFactory.getLogger(SeedOrchestratorCommandService.class);

    private static final List<String> SEED_AFFECTED_CACHES = List.of(
        "pokemon",
        "species",
        "moves",
        "items",
        "abilities",
        "natures",
        "types",
        "typeEffectiveness"
    );

    private final TypeSeedService typeSeedService;
    private final ItemSeedService itemSeedService;
    private final MoveSeedService moveSeedService;
    private final NatureSeedService natureSeedService;
    private final AbilitySeedService abilitySeedService;
    private final SpeciesSeedService speciesSeedService;
    private final PokemonSeedService pokemonSeedService;

    private final CacheManager cacheManager;

    @Transactional
    public SeedResultDto seed() {
        clearSeedData();

        SeedResultDto result = typeSeedService.seed()
            .add(natureSeedService.seed())
            .add(abilitySeedService.seed())
            .add(itemSeedService.seed())
            .add(moveSeedService.seed())
            .add(speciesSeedService.seed())
            .add(pokemonSeedService.seed());

        evictAllCaches();

        log.info("Seed completed: {} entries, {} errors", result.entriesAdded(), result.errors());

        return result;
    }

    private void clearSeedData() {
        pokemonSeedService.clearSeedData();
        speciesSeedService.clearSeedData();
        abilitySeedService.clearSeedData();
        natureSeedService.clearSeedData();
        moveSeedService.clearSeedData();
        itemSeedService.clearSeedData();
        typeSeedService.clearSeedData();

        evictAllCaches();
    }

    private void evictAllCaches() {
        for (String name : SEED_AFFECTED_CACHES) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
        log.info("Evicted {} caches", SEED_AFFECTED_CACHES.size());
    }
}
