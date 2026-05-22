package com.poketeambuilder.services.command;

import java.util.List;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.services.seed.TypeSeedService;
import com.poketeambuilder.services.seed.ItemSeedService;
import com.poketeambuilder.services.seed.MoveSeedService;
import com.poketeambuilder.services.seed.NatureSeedService;
import com.poketeambuilder.services.seed.AbilitySeedService;
import com.poketeambuilder.services.seed.PokemonSeedService;
import com.poketeambuilder.services.seed.SpeciesSeedService;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * Top-level entry point for the PokeAPI ingest pipeline. Calls each {@code *SeedService} in
 * dependency order:
 *
 * <pre>type -> nature -> ability -> item - move -> species -> pokemon</pre>
 *
 * <p>Intentionally not {@code @Transactional}. Each child seed service manages its
 * own short transactions internally via {@code TransactionTemplate}, so the orchestrator never
 * holds a DB connection while waiting on PokeAPI HTTP calls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeedOrchestratorCommandService {

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

    /** Runs the full seed pipeline. Returns the aggregated entries-added / errors counts. */
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

    /** Clears every persisted seed in the reverse dependency order to respect FK constraints. */
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
