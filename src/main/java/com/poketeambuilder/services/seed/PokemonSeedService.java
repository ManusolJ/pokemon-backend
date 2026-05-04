package com.poketeambuilder.services.seed;

import com.poketeambuilder.entities.Type;
import com.poketeambuilder.entities.Move;
import com.poketeambuilder.entities.Ability;
import com.poketeambuilder.entities.Pokemon;
import com.poketeambuilder.entities.PokemonMove;
import com.poketeambuilder.entities.PokemonAbility;
import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.entities.compositeIDs.PokemonMoveId;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonApiDto;
import com.poketeambuilder.dtos.pokeapi.type.PokemonTypeApiDto;
import com.poketeambuilder.dtos.pokeapi.move.PokemonMoveApiDto;
import com.poketeambuilder.dtos.pokeapi.move.VersionGroupDetail;
import com.poketeambuilder.dtos.pokeapi.ability.PokemonAbilityApiDto;

import com.poketeambuilder.mappers.implementation.PokemonMapper;

import com.poketeambuilder.repositories.MoveRepository;
import com.poketeambuilder.repositories.TypeRepository;
import com.poketeambuilder.repositories.AbilityRepository;
import com.poketeambuilder.repositories.PokemonRepository;
import com.poketeambuilder.repositories.SpeciesRepository;
import com.poketeambuilder.repositories.PokemonMoveRepository;
import com.poketeambuilder.repositories.PokemonAbilityRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PokemonSeedService {

    private static final Logger log = LoggerFactory.getLogger(PokemonSeedService.class);

    private final PokemonMapper pokemonMapper;
    private final TypeRepository typeRepository;
    private final MoveRepository moveRepository;
    private final AbilityRepository abilityRepository;
    private final PokemonRepository pokemonRepository;
    private final SpeciesRepository speciesRepository;
    private final PokemonMoveRepository pokemonMoveRepository;
    private final PokemonAbilityRepository pokemonAbilityRepository;

    private final PokeApiClient pokeApiClient;

    private static final int MAX_CANON_MOVE_ID = 10000;
    private static final int MAX_CANON_POKEMON_ID = 10000;

    private static final String POKEMON_ENDPOINT = "/pokemon";

    @Transactional
    public SeedResultDto seed() {
        int errors = 0;

        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(POKEMON_ENDPOINT);

        List<PokemonApiDto> pokemonDtos = new ArrayList<>();

        for (PokeApiResource resource : resources) {
            try {
                PokemonApiDto dto = pokeApiClient.fetchResource(resource.url(), PokemonApiDto.class);

                if (dto.id() <= MAX_CANON_POKEMON_ID) {
                    pokemonDtos.add(dto);
                }
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch pokemon resource: {}", resource.url(), e);
            }
        }

        List<Pokemon> entities = new ArrayList<>();

        for (PokemonApiDto dto : pokemonDtos) {
            try {
                Pokemon pokemon = pokemonMapper.toEntity(dto);

                setSpecies(pokemon, dto);
                setTypes(pokemon, dto);

                entities.add(pokemon);
            } catch (Exception e) {
                errors++;
                log.error("Failed to map pokemon DTO: {}", dto.name(), e);
            }
        }

        pokemonRepository.saveAllAndFlush(entities);

        log.info("Seeded {} pokemon ({} errors)", entities.size(), errors);

        int abilitiesSeeded = seedPokemonAbilities(pokemonDtos);

        log.info("Seeded {} pokemon-ability entries", abilitiesSeeded);

        int movesSeeded = seedPokemonMoves(pokemonDtos);

        log.info("Seeded {} pokemon-move entries", movesSeeded);

        return new SeedResultDto(entities.size() + abilitiesSeeded + movesSeeded, errors);
    }

    public void clearSeedData() {
        pokemonMoveRepository.deleteAllInBatch();
        pokemonAbilityRepository.deleteAllInBatch();
        pokemonRepository.deleteAllInBatch();
    }

    private void setSpecies(Pokemon pokemon, PokemonApiDto dto) {
        Integer speciesId = dto.species().extractId();
        PokemonSpecies species = speciesRepository.getReferenceById(speciesId);

        pokemon.setSpecies(species);
    }

    private void setTypes(Pokemon pokemon, PokemonApiDto dto) {
        if (dto.types() == null || dto.types().isEmpty()) {
            throw new IllegalArgumentException("Pokemon must have at least one type: " + dto.name());
        }

        List<PokemonTypeApiDto> sorted = dto.types().stream()
            .sorted(Comparator.comparing(PokemonTypeApiDto::slot))
            .toList();

        Type primaryType = typeRepository.getReferenceById(sorted.getFirst().type().extractId());
        pokemon.setPrimaryType(primaryType);

        if (sorted.size() > 1) {
            Type secondaryType = typeRepository.getReferenceById(sorted.getLast().type().extractId());
            pokemon.setSecondaryType(secondaryType);
        }
    }


    private int seedPokemonAbilities(List<PokemonApiDto> pokemonDtos) {
        List<PokemonAbility> entries = new ArrayList<>();

        for (PokemonApiDto dto : pokemonDtos) {
            if (dto.abilities() == null) {
                throw new IllegalArgumentException("Pokemon abilities cannot be null: " + dto.name());
            }

            Pokemon pokemon = pokemonRepository.getReferenceById(dto.id());

            for (PokemonAbilityApiDto abilityDto : dto.abilities()) {
                try {
                    Integer abilityId = abilityDto.ability().extractId();
                    Ability ability = abilityRepository.getReferenceById(abilityId);

                    entries.add(PokemonAbility.builder()
                        .pokemon(pokemon)
                        .ability(ability)
                        .slot(abilityDto.slot())
                        .isHidden(abilityDto.isHidden())
                        .build());
                } catch (Exception e) {
                    log.error("Failed to map ability for pokemon {}: {}", dto.name(), abilityDto, e);
                }
            }
        }

        pokemonAbilityRepository.saveAll(entries);

        return entries.size();
    }

    private int seedPokemonMoves(List<PokemonApiDto> pokemonDtos) {
        List<PokemonMove> entries = new ArrayList<>();

        for (PokemonApiDto dto : pokemonDtos) {
            if (dto.moves() == null) {
                throw new IllegalArgumentException("Pokemon moves cannot be null: " + dto.name());
            }

            Pokemon pokemon = pokemonRepository.getReferenceById(dto.id());

            for (PokemonMoveApiDto moveDto : dto.moves()) {
                Integer moveId = moveDto.move().extractId();

                if (moveId == null || moveId > MAX_CANON_MOVE_ID) {
                    continue;
                }

                try {
                    Move move = moveRepository.getReferenceById(moveId);

                    Map<String, VersionGroupDetail> latestByMethod = getLatestByMethod(moveDto);

                    for (VersionGroupDetail detail : latestByMethod.values()) {
                        String learnMethod = detail.moveLearnMethod().name();

                        entries.add(PokemonMove.builder()
                            .id(new PokemonMoveId(moveId, dto.id(), learnMethod))
                            .pokemon(pokemon)
                            .move(move)
                            .levelLearnedAt(detail.levelLearnedAt())
                            .build());
                    }
                } catch (Exception e) {
                    log.error("Failed to map move for pokemon {}: {}", dto.name(), moveDto.move().name(), e);
                }
            }
        }

        pokemonMoveRepository.saveAll(entries);

        return entries.size();
    }

    private Map<String, VersionGroupDetail> getLatestByMethod(PokemonMoveApiDto moveDto) {
        Map<String, VersionGroupDetail> latestByMethod = new HashMap<>();

        if (moveDto.versionGroupDetails() == null) {
            return latestByMethod;
        }

        for (VersionGroupDetail detail : moveDto.versionGroupDetails()) {
            if (detail.moveLearnMethod() == null) {
                continue;
            }

            String method = detail.moveLearnMethod().name();

            latestByMethod.merge(method, detail, (existing, candidate) -> {
                int existingOrder = existing.order() != null ? existing.order() : 0;
                int candidateOrder = candidate.order() != null ? candidate.order() : 0;

                return candidateOrder >= existingOrder ? candidate : existing;
            });
        }

        return latestByMethod;
    }
}