package com.poketeambuilder.services.query;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import com.poketeambuilder.entities.Pokemon;
import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.dtos.front.ability.AbilityEmbedDto;
import com.poketeambuilder.dtos.front.ability.AbilitySummaryDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonReadDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonFilterDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.PokemonMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.PokemonRepository;
import com.poketeambuilder.repositories.PokemonAbilityRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.stereotype.Service;

import org.springframework.cache.CacheManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaQuery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Read access to {@link Pokemon} (form-level).
 */
@Service
@Validated
public class PokemonQueryService extends AbstractQueryService<Pokemon, Integer, PokemonFilterDto, PokemonReadDto> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRIMARY_TYPE_ID = "primaryType.id";
    private static final String FIELD_SECONDARY_TYPE_ID = "secondaryType.id";
    private static final String JOIN_PRIMARY_TYPE = "primaryType";
    private static final String JOIN_SECONDARY_TYPE = "secondaryType";
    private static final String TYPE_ID = "id";
    private static final String FIELD_IS_DEFAULT_FORM = "isDefaultForm";
    private static final String FIELD_HEIGHT = "height";
    private static final String FIELD_WEIGHT = "weight";
    private static final String FIELD_BASE_HP = "baseHp";
    private static final String FIELD_BASE_ATK = "baseAtk";
    private static final String FIELD_BASE_DEF = "baseDef";
    private static final String FIELD_BASE_SP_ATK = "baseSpAtk";
    private static final String FIELD_BASE_SP_DEF = "baseSpDef";
    private static final String FIELD_BASE_SPEED = "baseSpeed";

    private static final String JOIN_SPECIES = "species";
    private static final String SPECIES_GENERATION = "generation";
    private static final String SPECIES_NATIONAL_DEX = "nationalDexNumber";
    private static final String SPECIES_IS_BABY = "isBaby";
    private static final String SPECIES_IS_MYTHICAL = "isMythical";
    private static final String SPECIES_IS_LEGENDARY = "isLegendary";
    private static final String SPECIES_GENDER_RATE = "genderRate";
    private static final String SPECIES_BASE_HAPPINESS = "baseHappiness";
    private static final String SPECIES_GROWTH_RATE = "growthRate";
    private static final String SPECIES_EGG_GROUP_1 = "eggGroup1";
    private static final String SPECIES_EGG_GROUP_2 = "eggGroup2";
    private static final String SPECIES_PREVIOUS_EVOLUTION = "previousEvolution";
    private static final String SPECIES_EVOLUTION_TRIGGER = "evolutionTrigger";
    private static final String SPECIES_EVOLUTION_ITEM = "evolutionItem";
    private static final String SPECIES_EVOLUTION_TIME_OF_DAY = "evolutionTimeOfDay";
    private static final String SPECIES_EVOLUTION_MIN_LEVEL = "evolutionMinLevel";

    private static final String EVOLVES_WITH_HAPPINESS_TRIGGER = "happiness";

    private static final int GENDERLESS_RATE = -1;

    private final PokemonMapper pokemonMapper;
    private final PokemonRepository pokemonRepository;
    private final PokemonAbilityRepository pokemonAbilityRepository;

    public PokemonQueryService(CacheManager cacheManager, PokemonMapper pokemonMapper, PokemonRepository pokemonRepository, PokemonAbilityRepository pokemonAbilityRepository) {
        super(cacheManager);
        this.pokemonMapper = pokemonMapper;
        this.pokemonRepository = pokemonRepository;
        this.pokemonAbilityRepository = pokemonAbilityRepository;
    }

    @Override
    protected String getEntityName() {
        return "Pokemon";
    }

    @Override
    protected String getCacheName() {
        return "pokemon";
    }

    @Override
    protected ReadMapper<Pokemon, PokemonReadDto> getMapper() {
        return pokemonMapper;
    }

    @Override
    protected BaseRepository<Pokemon, Integer> getRepository() {
        return pokemonRepository;
    }

    @Override
    protected void applyFetches(Root<Pokemon> root, CriteriaQuery<?> query) {
        root.fetch("primaryType");
        root.fetch("secondaryType", JoinType.LEFT);
        root.fetch("species");
        query.distinct(true);
    }

    @Override
    public Page<PokemonReadDto> filterEntities(@Valid @NotNull PokemonFilterDto filter, @NotNull Pageable pageable) {
        Page<PokemonReadDto> page = super.filterEntities(filter, pageable);

        List<Integer> ids = page.getContent().stream().map(PokemonReadDto::id).toList();
        if (ids.isEmpty()) {
            return page;
        }

        Map<Integer, List<AbilityEmbedDto>> abilitiesMap = fetchAbilitiesForPokemon(ids);

        List<PokemonReadDto> enriched = page.getContent().stream()
                .map(dto -> withAbilities(dto, abilitiesMap.getOrDefault(dto.id(), List.of())))
                .toList();

        return new PageImpl<>(enriched, pageable, page.getTotalElements());
    }

    @Override
    public PokemonReadDto findById(@NotNull Integer id) {
        PokemonReadDto dto = super.findById(id);
        return withAbilities(dto, fetchAbilitiesForPokemon(List.of(id)).getOrDefault(id, List.of()));
    }

    /** Compact projection for the form picker / embeds. */
    public Page<PokemonSummaryDto> filterSummaries(@Valid @NotNull PokemonFilterDto filter, @NotNull Pageable pageable) {
        return filterAndMap(filter, pageable, pokemonMapper::toSummaryDto);
    }

    @Override
    protected Specification<Pokemon> buildSpecification(@NotNull PokemonFilterDto filter) {
        SpecificationBuilder<Pokemon> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getId() != null) {
            builder.with(FIELD_ID, filter.getId(), SearchOperation.EQUAL);
        }
        if (filter.getName() != null && !filter.getName().isBlank()) {
            builder.with(FIELD_NAME, filter.getName(), SearchOperation.LIKE);
        }
        if (filter.getNameExact() != null && !filter.getNameExact().isBlank()) {
            builder.with(FIELD_NAME, filter.getNameExact(), SearchOperation.EQUAL);
        }
        if (filter.getIsDefaultForm() != null) {
            builder.with(FIELD_IS_DEFAULT_FORM, filter.getIsDefaultForm(), SearchOperation.EQUAL);
        }
        if (filter.getPrimaryTypeId() != null) {
            builder.with(FIELD_PRIMARY_TYPE_ID, filter.getPrimaryTypeId(), SearchOperation.EQUAL);
        }
        if (filter.getSecondaryTypeId() != null) {
            builder.with(FIELD_SECONDARY_TYPE_ID, filter.getSecondaryTypeId(), SearchOperation.EQUAL);
        }

        addRange(builder, FIELD_HEIGHT, filter.getMinHeight(), filter.getMaxHeight());
        addRange(builder, FIELD_WEIGHT, filter.getMinWeight(), filter.getMaxWeight());
        addRange(builder, FIELD_BASE_HP, filter.getMinBaseHp(), filter.getMaxBaseHp());
        addRange(builder, FIELD_BASE_ATK, filter.getMinBaseAtk(), filter.getMaxBaseAtk());
        addRange(builder, FIELD_BASE_DEF, filter.getMinBaseDef(), filter.getMaxBaseDef());
        addRange(builder, FIELD_BASE_SP_ATK, filter.getMinBaseSpAtk(), filter.getMaxBaseSpAtk());
        addRange(builder, FIELD_BASE_SP_DEF, filter.getMinBaseSpDef(), filter.getMaxBaseSpDef());
        addRange(builder, FIELD_BASE_SPEED, filter.getMinBaseSpeed(), filter.getMaxBaseSpeed());

        return addTypeFilter(addSpeciesFilters(builder.build(), filter), filter);
    }

    /**
     * Adds a slot-agnostic filter for {@link PokemonFilterDto#getTypeIds()}.
     * For each requested type id the Pokémon must have it in either the
     * primary or secondary slot.*/
    private Specification<Pokemon> addTypeFilter(Specification<Pokemon> spec, PokemonFilterDto filter) {
        List<Integer> typeIds = filter.getTypeIds();
        if (typeIds == null || typeIds.isEmpty()) {
            return spec;
        }

        return spec.and((root, query, cb) -> {
            Join<Pokemon, ?> primary = root.join(JOIN_PRIMARY_TYPE, JoinType.LEFT);
            Join<Pokemon, ?> secondary = root.join(JOIN_SECONDARY_TYPE, JoinType.LEFT);

            Predicate predicates = cb.conjunction();
            for (Integer typeId : typeIds) {
                if (typeId == null) {
                    continue;
                }
                predicates = cb.and(predicates, cb.or(
                        cb.equal(primary.get(TYPE_ID), typeId),
                        cb.equal(secondary.get(TYPE_ID), typeId)));
            }
            return predicates;
        });
    }

    /**
     * Joins the form's species and adds species-level predicates (Pokédex shape, evolution
     * mechanic, egg groups, …) on top of the form-level base spec.
     */
    private Specification<Pokemon> addSpeciesFilters(Specification<Pokemon> spec, PokemonFilterDto filter) {
        if (!hasSpeciesFilter(filter)) {
            return spec;
        }

        return spec.and((root, query, cb) -> {
            Join<Pokemon, PokemonSpecies> species = root.join(JOIN_SPECIES);
            Predicate predicates = cb.conjunction();

            if (filter.getGeneration() != null) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_GENERATION), filter.getGeneration()));
            }
            if (filter.getNationalDexNumber() != null) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_NATIONAL_DEX), filter.getNationalDexNumber()));
            }
            if (filter.getIsBaby() != null) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_IS_BABY), filter.getIsBaby()));
            }
            if (filter.getIsMythical() != null) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_IS_MYTHICAL), filter.getIsMythical()));
            }
            if (filter.getIsLegendary() != null) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_IS_LEGENDARY), filter.getIsLegendary()));
            }

            if (filter.getIsGenderless() != null && filter.getIsGenderless()) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_GENDER_RATE), GENDERLESS_RATE));
            } else if (filter.getIsGenderless() != null) {
                predicates = cb.and(predicates, cb.notEqual(species.get(SPECIES_GENDER_RATE), GENDERLESS_RATE));
            }

            if (filter.getMinGenderRate() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(species.get(SPECIES_GENDER_RATE), filter.getMinGenderRate()));
            }
            if (filter.getMaxGenderRate() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(species.get(SPECIES_GENDER_RATE), filter.getMaxGenderRate()));
            }
            if (filter.getMinBaseHappiness() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(species.get(SPECIES_BASE_HAPPINESS), filter.getMinBaseHappiness()));
            }
            if (filter.getMaxBaseHappiness() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(species.get(SPECIES_BASE_HAPPINESS), filter.getMaxBaseHappiness()));
            }
            if (filter.getGrowthRate() != null && !filter.getGrowthRate().isBlank()) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_GROWTH_RATE), filter.getGrowthRate()));
            }

            if (filter.getHasPreviousEvolution() != null && filter.getHasPreviousEvolution()) {
                predicates = cb.and(predicates, cb.isNotNull(species.get(SPECIES_PREVIOUS_EVOLUTION)));
            } else if (filter.getHasPreviousEvolution() != null) {
                predicates = cb.and(predicates, cb.isNull(species.get(SPECIES_PREVIOUS_EVOLUTION)));
            }

            List<String> eggGroups = filter.getEggGroups();
            if (eggGroups != null && !eggGroups.isEmpty()) {
                predicates = cb.and(predicates, cb.or(
                        species.get(SPECIES_EGG_GROUP_1).in(eggGroups),
                        species.get(SPECIES_EGG_GROUP_2).in(eggGroups)));
            }

            if (filter.getEvolutionTrigger() != null && !filter.getEvolutionTrigger().isBlank()) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_EVOLUTION_TRIGGER), filter.getEvolutionTrigger()));
            }
            if (filter.getEvolutionItem() != null && !filter.getEvolutionItem().isBlank()) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_EVOLUTION_ITEM), filter.getEvolutionItem()));
            }
            if (filter.getEvolutionTimeOfDay() != null && !filter.getEvolutionTimeOfDay().isBlank()) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_EVOLUTION_TIME_OF_DAY), filter.getEvolutionTimeOfDay()));
            }
            if (Boolean.TRUE.equals(filter.getEvolvesWithItem())) {
                predicates = cb.and(predicates, cb.isNotNull(species.get(SPECIES_EVOLUTION_ITEM)));
            }
            if (Boolean.TRUE.equals(filter.getEvolvesWithLevelUp())) {
                predicates = cb.and(predicates, cb.isNotNull(species.get(SPECIES_EVOLUTION_MIN_LEVEL)));
            }
            if (Boolean.TRUE.equals(filter.getEvolvesWithHappiness())) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_EVOLUTION_TRIGGER), EVOLVES_WITH_HAPPINESS_TRIGGER));
            }

            if (filter.getMinEvolutionLevel() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(species.get(SPECIES_EVOLUTION_MIN_LEVEL), filter.getMinEvolutionLevel()));
            }
            if (filter.getMaxEvolutionLevel() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(species.get(SPECIES_EVOLUTION_MIN_LEVEL), filter.getMaxEvolutionLevel()));
            }

            return predicates;
        });
    }

    private static boolean hasSpeciesFilter(PokemonFilterDto filter) {
        return filter.getGeneration() != null
                || filter.getNationalDexNumber() != null
                || filter.getIsBaby() != null
                || filter.getIsMythical() != null
                || filter.getIsLegendary() != null
                || filter.getIsGenderless() != null
                || filter.getMinGenderRate() != null
                || filter.getMaxGenderRate() != null
                || filter.getMinBaseHappiness() != null
                || filter.getMaxBaseHappiness() != null
                || (filter.getGrowthRate() != null && !filter.getGrowthRate().isBlank())
                || (filter.getEggGroups() != null && !filter.getEggGroups().isEmpty())
                || filter.getHasPreviousEvolution() != null
                || (filter.getEvolutionTrigger() != null && !filter.getEvolutionTrigger().isBlank())
                || (filter.getEvolutionItem() != null && !filter.getEvolutionItem().isBlank())
                || (filter.getEvolutionTimeOfDay() != null && !filter.getEvolutionTimeOfDay().isBlank())
                || filter.getEvolvesWithItem() != null
                || filter.getEvolvesWithLevelUp() != null
                || filter.getEvolvesWithHappiness() != null
                || filter.getMinEvolutionLevel() != null
                || filter.getMaxEvolutionLevel() != null;
    }

    private void addRange(SpecificationBuilder<Pokemon> builder, String field, Integer min, Integer max) {
        if (min != null) {
            builder.with(field, min, SearchOperation.GREATER_THAN_OR_EQUAL);
        }
        if (max != null) {
            builder.with(field, max, SearchOperation.LESS_THAN_OR_EQUAL);
        }
    }

    private Map<Integer, List<AbilityEmbedDto>> fetchAbilitiesForPokemon(List<Integer> pokemonIds) {
        return pokemonAbilityRepository.findByPokemonIdInWithAbility(pokemonIds).stream()
                .collect(Collectors.groupingBy(
                        pa -> pa.getId().getPokemonId(),
                        Collectors.mapping(pa -> new AbilityEmbedDto(
                                new AbilitySummaryDto(pa.getAbility().getId(), pa.getAbility().getName()),
                                pa.getIsHidden(),
                                pa.getSlot()
                        ), Collectors.toList())));
    }

    private PokemonReadDto withAbilities(PokemonReadDto dto, List<AbilityEmbedDto> abilities) {
        return new PokemonReadDto(
                dto.id(), dto.name(), dto.order(), dto.species(), dto.isDefaultForm(),
                dto.primaryType(), dto.secondaryType(),
                dto.baseHp(), dto.baseAtk(), dto.baseDef(), dto.baseSpAtk(), dto.baseSpDef(), dto.baseSpeed(),
                dto.heightInMeters(), dto.weightInKilograms(),
                abilities,
                dto.spriteDefault(), dto.spriteShiny(), dto.artworkUrl(), dto.artworkShiny());
    }
}
