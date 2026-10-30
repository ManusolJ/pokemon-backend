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

    /**
     * Runs the full seed pipeline. Returns the aggregated entries-added / errors counts.
     *
     * <p>The derived join tables - learnsets, ability slots and the effectiveness matrix - are
     * rebuilt by their seed services inside the same transaction that refills them, so they are
     * never visible half-empty and a failed run leaves the previous data in place. The catalogue
     * tables are upserted in place under PokeAPI's own ids: every one of them is referenced by
     * {@code team_pokemon} or {@code team_pokemon_move}, whose foreign keys carry no
     * {@code ON DELETE} action, so they can't be cleared while a single team exists.</p>
     *
     * <p>Caches are evicted however the run ends: entries read mid-run hold partial data and
     * would otherwise outlive a failure for up to a day.</p>
     */
    public SeedResultDto seed() {
        try {
            SeedResultDto result = typeSeedService.seed()
                    .add(natureSeedService.seed())
                    .add(abilitySeedService.seed())
                    .add(itemSeedService.seed())
                    .add(moveSeedService.seed())
                    .add(speciesSeedService.seed())
                    .add(pokemonSeedService.seed());

            log.info("Seed completed: {} entries, {} errors", result.entriesAdded(), result.errors());

            return result;
        } finally {
            evictAllCaches();
        }
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
