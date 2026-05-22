package com.poketeambuilder.services.seed;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

import com.poketeambuilder.entities.Type;
import com.poketeambuilder.entities.Move;
import com.poketeambuilder.entities.Ability;
import com.poketeambuilder.entities.Pokemon;
import com.poketeambuilder.entities.PokemonMove;
import com.poketeambuilder.entities.PokemonAbility;
import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.entities.compositeIDs.PokemonMoveId;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the {@code pokemon}, {@code pokemon_ability}, and {@code pokemon_move} tables from
 * PokeAPI. Cosmetic alt-forms (gmax, totem, cap variants, etc) are dropped to keep the table
 * focused on competitively-relevant forms.
 */
@Slf4j
@Service
public class PokemonSeedService {

    private static final String POKEMON_ENDPOINT = "/pokemon";
    private static final int NON_CANON_MOVE_ID_THRESHOLD = 10000;

    private static final Set<String> COSMETIC_FORM_SUFFIXES = Set.of(
            "-gmax",
            "-totem",
            "-eternamax",
            "-cap",
            "-cosplay",
            "-rock-star",
            "-belle",
            "-pop-star",
            "-phd",
            "-libre",
            "-starter"
    );

    private final PokemonMapper pokemonMapper;
    private final TypeRepository typeRepository;
    private final MoveRepository moveRepository;
    private final AbilityRepository abilityRepository;
    private final PokemonRepository pokemonRepository;
    private final SpeciesRepository speciesRepository;
    private final PokemonMoveRepository pokemonMoveRepository;
    private final PokemonAbilityRepository pokemonAbilityRepository;

    private final PokeApiClient pokeApiClient;
    private final TransactionTemplate transactionTemplate;

    public PokemonSeedService(PokemonMapper pokemonMapper, TypeRepository typeRepository, MoveRepository moveRepository, AbilityRepository abilityRepository, PokemonRepository pokemonRepository,
                              SpeciesRepository speciesRepository, PokemonMoveRepository pokemonMoveRepository,
                              PokemonAbilityRepository pokemonAbilityRepository,
                              PokeApiClient pokeApiClient, TransactionTemplate transactionTemplate) {
        this.pokeApiClient = pokeApiClient;
        this.pokemonMapper = pokemonMapper;
        this.typeRepository = typeRepository;
        this.moveRepository = moveRepository;
        this.abilityRepository = abilityRepository;
        this.pokemonRepository = pokemonRepository;
        this.speciesRepository = speciesRepository;
        this.transactionTemplate = transactionTemplate;
        this.pokemonMoveRepository = pokemonMoveRepository;
        this.pokemonAbilityRepository = pokemonAbilityRepository;
    }

    public SeedResultDto seed() {
        FetchResult fetched = fetchAll();

        PersistResult forms = transactionTemplate.execute(status -> persistForms(fetched.apiDtos(), fetched.errors()));
        log.info("Seeded {} pokemon ({} errors so far)", forms.saved(), forms.errors());

        PersistResult abilities = transactionTemplate.execute(status -> persistAbilities(fetched.apiDtos(), forms.errors()));
        log.info("Seeded {} pokemon-ability entries ({} errors so far)", abilities.saved(), abilities.errors());

        PersistResult moves = transactionTemplate.execute(status -> persistMoves(fetched.apiDtos(), abilities.errors()));
        log.info("Seeded {} pokemon-move entries ({} errors total)", moves.saved(), moves.errors());

        return SeedResultDto.of(forms.saved() + abilities.saved() + moves.saved(), moves.errors());
    }

    @Transactional
    public void clearSeedData() {
        pokemonMoveRepository.deleteAllInBatch();
        pokemonAbilityRepository.deleteAllInBatch();
        pokemonRepository.deleteAllInBatch();
    }

    private FetchResult fetchAll() {
        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(POKEMON_ENDPOINT);

        List<PokemonApiDto> pokemonDtos = new ArrayList<>();
        int errors = 0;

        for (PokeApiResource resource : resources) {
            try {
                PokemonApiDto dto = pokeApiClient.fetchResource(resource.url(), PokemonApiDto.class);
                if (!isCosmeticForm(dto)) {
                    pokemonDtos.add(dto);
                }
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch pokemon resource: {}", resource.url(), e);
            }
        }

        return new FetchResult(pokemonDtos, errors);
    }

    private PersistResult persistForms(List<PokemonApiDto> pokemonDtos, int initialErrors) {
        List<Pokemon> entities = new ArrayList<>();
        int errors = initialErrors;

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

        return new PersistResult(entities.size(), errors);
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

    private PersistResult persistAbilities(List<PokemonApiDto> pokemonDtos, int initialErrors) {
        List<PokemonAbility> entries = new ArrayList<>();
        int errors = initialErrors;

        for (PokemonApiDto dto : pokemonDtos) {
            if (dto.abilities() == null) {
                errors++;
                log.error("Pokemon '{}' has null abilities list; skipping", dto.name());
                continue;
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
                    errors++;
                    log.error("Failed to map ability for pokemon {}: {}", dto.name(), abilityDto, e);
                }
            }
        }

        pokemonAbilityRepository.saveAll(entries);

        return new PersistResult(entries.size(), errors);
    }

    private PersistResult persistMoves(List<PokemonApiDto> pokemonDtos, int initialErrors) {
        List<PokemonMove> entries = new ArrayList<>();
        int errors = initialErrors;

        Set<Integer> seededMoveIds = Set.copyOf(moveRepository.findAllIds());

        for (PokemonApiDto dto : pokemonDtos) {
            if (dto.moves() == null) {
                errors++;
                log.error("Pokemon '{}' has null moves list; skipping", dto.name());
                continue;
            }

            Pokemon pokemon = pokemonRepository.getReferenceById(dto.id());

            for (PokemonMoveApiDto moveDto : dto.moves()) {
                Integer moveId = moveDto.move().extractId();

                if (moveId == null || moveId > NON_CANON_MOVE_ID_THRESHOLD) {
                    continue;
                }

                if (!seededMoveIds.contains(moveId)) {
                    log.debug("Skipping move {} for pokemon {}: not in seeded move set",
                            moveDto.move().name(), dto.name());
                    continue;
                }

                try {
                    Move move = moveRepository.getReferenceById(moveId);

                    Map<String, VersionGroupDetail> latestByMethod = getLatestByMethod(moveDto);

                    for (VersionGroupDetail detail : latestByMethod.values()) {
                        String learnMethod = detail.moveLearnMethod().name();

                        entries.add(PokemonMove.builder()
                                .id(new PokemonMoveId(dto.id(), moveId, learnMethod))
                                .pokemon(pokemon)
                                .move(move)
                                .levelLearnedAt(detail.levelLearnedAt())
                                .build());
                    }
                } catch (Exception e) {
                    errors++;
                    log.error("Failed to map move for pokemon {}: {}", dto.name(), moveDto.move().name(), e);
                }
            }
        }

        pokemonMoveRepository.saveAll(entries);

        return new PersistResult(entries.size(), errors);
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

    private boolean isCosmeticForm(PokemonApiDto dto) {
        if (Boolean.TRUE.equals(dto.isDefault())) {
            return false;
        }
        String name = dto.name();
        return COSMETIC_FORM_SUFFIXES.stream().anyMatch(name::endsWith);
    }

    private record FetchResult(List<PokemonApiDto> apiDtos, int errors) {}

    private record PersistResult(int saved, int errors) {}
}
