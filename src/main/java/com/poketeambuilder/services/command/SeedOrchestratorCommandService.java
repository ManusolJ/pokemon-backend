package com.poketeambuilder.services.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poketeambuilder.dtos.front.admin.seed.SeedResult;
import com.poketeambuilder.services.seed.AbilitySeedService;
import com.poketeambuilder.services.seed.ItemSeedService;
import com.poketeambuilder.services.seed.MoveSeedService;
import com.poketeambuilder.services.seed.NatureSeedService;
import com.poketeambuilder.services.seed.PokemonSeedService;
import com.poketeambuilder.services.seed.SpeciesSeedService;
import com.poketeambuilder.services.seed.TypeSeedService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedOrchestratorCommandService {

    private final static Logger log = LoggerFactory.getLogger(SeedOrchestratorCommandService.class);

    private final TypeSeedService typeSeedService;
    private final ItemSeedService itemSeedService;
    private final MoveSeedService moveSeedService;
    private final NatureSeedService natureSeedService;
    private final AbilitySeedService abilitySeedService;
    private final SpeciesSeedService speciesSeedService;
    private final PokemonSeedService pokemonSeedService;

    @Transactional
    public SeedResult seed() {
    clearSeedData();

        SeedResult result = typeSeedService.seed()
            .add(natureSeedService.seed())
            .add(abilitySeedService.seed())
            .add(itemSeedService.seed())
            .add(moveSeedService.seed())
            .add(speciesSeedService.seed())
            .add(pokemonSeedService.seed());

        log.info("Seed completed: {} entries, {} errors", result.entriesAdded(), result.errors());

        return result;
    }

    @Transactional
    private void clearSeedData() {
        pokemonSeedService.clearSeedData();
        speciesSeedService.clearSeedData();
        abilitySeedService.clearSeedData();
        natureSeedService.clearSeedData();
        moveSeedService.clearSeedData();
        itemSeedService.clearSeedData();
        typeSeedService.clearSeedData();
    }
}
