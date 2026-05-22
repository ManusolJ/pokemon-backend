package com.poketeambuilder.services.query;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.poketeambuilder.entities.Pokemon;
import com.poketeambuilder.entities.PokemonSpecies;
import com.poketeambuilder.entities.Type;

import com.poketeambuilder.dtos.front.type.single.TypeReadDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonFilterDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesReadDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.SpeciesMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.PokemonRepository;
import com.poketeambuilder.repositories.SpeciesRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.cache.CacheManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import org.springframework.validation.annotation.Validated;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Read access to {@link PokemonSpecies}.
 */
@Service
@Validated
public class SpeciesQueryService extends AbstractQueryService<PokemonSpecies, Integer, PokemonFilterDto, PokemonSpeciesReadDto> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_GENERATION = "generation";
    private static final String FIELD_NATIONAL_DEX = "nationalDexNumber";
    private static final String FIELD_GROWTH_RATE = "growthRate";
    private static final String FIELD_GENDER_RATE = "genderRate";
    private static final String FIELD_BASE_HAPPINESS = "baseHappiness";
    private static final String FIELD_IS_BABY = "isBaby";
    private static final String FIELD_IS_MYTHICAL = "isMythical";
    private static final String FIELD_IS_LEGENDARY = "isLegendary";
    private static final String FIELD_EGG_GROUP_1 = "eggGroup1";
    private static final String FIELD_EGG_GROUP_2 = "eggGroup2";
    private static final String FIELD_EVOLUTION_ITEM = "evolutionItem";
    private static final String FIELD_EVOLUTION_TRIGGER = "evolutionTrigger";
    private static final String FIELD_PREVIOUS_EVOLUTION = "previousEvolution";
    private static final String FIELD_EVOLUTION_MIN_LEVEL = "evolutionMinLevel";
    private static final String FIELD_EVOLUTION_TIME_OF_DAY = "evolutionTimeOfDay";

    private static final String POKEMON_SPECIES = "species";
    private static final String POKEMON_PRIMARY_TYPE = "primaryType";
    private static final String POKEMON_SECONDARY_TYPE = "secondaryType";
    private static final String POKEMON_IS_DEFAULT_FORM = "isDefaultForm";
    private static final String POKEMON_HEIGHT = "height";
    private static final String POKEMON_WEIGHT = "weight";
    private static final String POKEMON_BASE_HP = "baseHp";
    private static final String POKEMON_BASE_ATK = "baseAtk";
    private static final String POKEMON_BASE_DEF = "baseDef";
    private static final String POKEMON_BASE_SP_ATK = "baseSpAtk";
    private static final String POKEMON_BASE_SP_DEF = "baseSpDef";
    private static final String POKEMON_BASE_SPEED = "baseSpeed";

    private static final String EVOLVES_WITH_HAPPINESS_TRIGGER = "happiness";

    private static final int GENDERLESS_RATE = -1;

    private final SpeciesMapper speciesMapper;
    private final SpeciesRepository speciesRepository;
    private final PokemonRepository pokemonRepository;

    public SpeciesQueryService(
            CacheManager cacheManager,
            SpeciesMapper speciesMapper,
            SpeciesRepository speciesRepository,
            PokemonRepository pokemonRepository) {
        super(cacheManager);
        this.speciesMapper = speciesMapper;
        this.speciesRepository = speciesRepository;
        this.pokemonRepository = pokemonRepository;
    }

    @Override
    protected String getEntityName() {
        return "Species";
    }

    @Override
    protected String getCacheName() {
        return "species";
    }

    @Override
    protected ReadMapper<PokemonSpecies, PokemonSpeciesReadDto> getMapper() {
        return speciesMapper;
    }

    @Override
    protected BaseRepository<PokemonSpecies, Integer> getRepository() {
        return speciesRepository;
    }

    @Override
    protected void applyFetches(Root<PokemonSpecies> root, CriteriaQuery<?> query) {
        root.fetch(FIELD_PREVIOUS_EVOLUTION, JoinType.LEFT);
        query.distinct(true);
    }

    /**
     * Pokédex listing: pages species rows and enriches each with the default form's types
     * and sprite. One species query + one Pokemon query per page; no n+1.
     */
    public Page<PokemonSpeciesSummaryDto> filterSummaries(@Valid @NotNull PokemonFilterDto filter, @NotNull Pageable pageable) {
        Specification<PokemonSpecies> spec = buildSpecification(filter);

        Page<PokemonSpecies> page = speciesRepository.findAll(spec, pageable);

        List<PokemonSpecies> speciesList = page.getContent();
        if (speciesList.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, page.getTotalElements());
        }

        List<Integer> speciesIds = speciesList.stream().map(PokemonSpecies::getId).toList();
        Map<Integer, Pokemon> defaultFormBySpeciesId = pokemonRepository
                .findDefaultFormsBySpeciesIdIn(speciesIds)
                .stream()
                .collect(Collectors.toMap(
                        pokemon -> pokemon.getSpecies().getId(),
                        pokemon -> pokemon,
                        (existing, replacement) -> existing));

        List<PokemonSpeciesSummaryDto> dtos = speciesList.stream()
                .map(species -> toEnrichedSummary(species, defaultFormBySpeciesId.get(species.getId())))
                .toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    private PokemonSpeciesSummaryDto toEnrichedSummary(PokemonSpecies species, Pokemon defaultForm) {
        TypeReadDto primary = null;
        TypeReadDto secondary = null;
        String spriteDefault = null;

        if (defaultForm != null) {
            primary = toTypeDto(defaultForm.getPrimaryType());
            secondary = toTypeDto(defaultForm.getSecondaryType());
            spriteDefault = defaultForm.getSpriteDefault();
        }

        return new PokemonSpeciesSummaryDto(
            species.getId(),
            species.getName(),
            species.getGenus(),
            species.getNationalDexNumber(),
            species.getSortOrder(),
            species.getGenderRate(),
            primary,
            secondary,
            spriteDefault
        );
    }

    private TypeReadDto toTypeDto(Type type) {
        return type == null ? null : new TypeReadDto(type.getId(), type.getName());
    }

    @Override
    protected Specification<PokemonSpecies> buildSpecification(@NotNull PokemonFilterDto filter) {
        SpecificationBuilder<PokemonSpecies> builder = new SpecificationBuilder<>();

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
        if (filter.getNationalDexNumber() != null) {
            builder.with(FIELD_NATIONAL_DEX, filter.getNationalDexNumber(), SearchOperation.EQUAL);
        }
        if (filter.getGeneration() != null) {
            builder.with(FIELD_GENERATION, filter.getGeneration(), SearchOperation.EQUAL);
        }
        if (filter.getIsBaby() != null) {
            builder.with(FIELD_IS_BABY, filter.getIsBaby(), SearchOperation.EQUAL);
        }
        if (filter.getIsMythical() != null) {
            builder.with(FIELD_IS_MYTHICAL, filter.getIsMythical(), SearchOperation.EQUAL);
        }
        if (filter.getIsLegendary() != null) {
            builder.with(FIELD_IS_LEGENDARY, filter.getIsLegendary(), SearchOperation.EQUAL);
        }
        if (filter.getGrowthRate() != null && !filter.getGrowthRate().isBlank()) {
            builder.with(FIELD_GROWTH_RATE, filter.getGrowthRate(), SearchOperation.EQUAL);
        }
        if (filter.getMinBaseHappiness() != null) {
            builder.with(FIELD_BASE_HAPPINESS, filter.getMinBaseHappiness(), SearchOperation.GREATER_THAN_OR_EQUAL);
        }
        if (filter.getMaxBaseHappiness() != null) {
            builder.with(FIELD_BASE_HAPPINESS, filter.getMaxBaseHappiness(), SearchOperation.LESS_THAN_OR_EQUAL);
        }
        if (filter.getMinGenderRate() != null) {
            builder.with(FIELD_GENDER_RATE, filter.getMinGenderRate(), SearchOperation.GREATER_THAN_OR_EQUAL);
        }
        if (filter.getMaxGenderRate() != null) {
            builder.with(FIELD_GENDER_RATE, filter.getMaxGenderRate(), SearchOperation.LESS_THAN_OR_EQUAL);
        }
        if (filter.getEvolutionTrigger() != null && !filter.getEvolutionTrigger().isBlank()) {
            builder.with(FIELD_EVOLUTION_TRIGGER, filter.getEvolutionTrigger(), SearchOperation.EQUAL);
        }
        if (filter.getEvolutionItem() != null && !filter.getEvolutionItem().isBlank()) {
            builder.with(FIELD_EVOLUTION_ITEM, filter.getEvolutionItem(), SearchOperation.EQUAL);
        }
        if (filter.getEvolutionTimeOfDay() != null && !filter.getEvolutionTimeOfDay().isBlank()) {
            builder.with(FIELD_EVOLUTION_TIME_OF_DAY, filter.getEvolutionTimeOfDay(), SearchOperation.EQUAL);
        }
        if (filter.getMinEvolutionLevel() != null) {
            builder.with(FIELD_EVOLUTION_MIN_LEVEL, filter.getMinEvolutionLevel(), SearchOperation.GREATER_THAN_OR_EQUAL);
        }
        if (filter.getMaxEvolutionLevel() != null) {
            builder.with(FIELD_EVOLUTION_MIN_LEVEL, filter.getMaxEvolutionLevel(), SearchOperation.LESS_THAN_OR_EQUAL);
        }

        Specification<PokemonSpecies> spec = builder.build();

        if (filter.getIsGenderless() != null && filter.getIsGenderless()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get(FIELD_GENDER_RATE), GENDERLESS_RATE));
        } else if (filter.getIsGenderless() != null) {
            spec = spec.and((root, query, cb) -> cb.notEqual(root.get(FIELD_GENDER_RATE), GENDERLESS_RATE));
        }

        if (filter.getHasPreviousEvolution() != null && filter.getHasPreviousEvolution()) {
            spec = spec.and((root, query, cb) -> cb.isNotNull(root.get(FIELD_PREVIOUS_EVOLUTION)));
        } else if (filter.getHasPreviousEvolution() != null) {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get(FIELD_PREVIOUS_EVOLUTION)));
        }

        List<String> eggGroups = filter.getEggGroups();
        if (eggGroups != null && !eggGroups.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    root.get(FIELD_EGG_GROUP_1).in(eggGroups),
                    root.get(FIELD_EGG_GROUP_2).in(eggGroups)));
        }

        if (Boolean.TRUE.equals(filter.getEvolvesWithItem())) {
            spec = spec.and((root, query, cb) -> cb.isNotNull(root.get(FIELD_EVOLUTION_ITEM)));
        }
        if (Boolean.TRUE.equals(filter.getEvolvesWithLevelUp())) {
            spec = spec.and((root, query, cb) -> cb.isNotNull(root.get(FIELD_EVOLUTION_MIN_LEVEL)));
        }
        if (Boolean.TRUE.equals(filter.getEvolvesWithHappiness())) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get(FIELD_EVOLUTION_TRIGGER), EVOLVES_WITH_HAPPINESS_TRIGGER));
        }

        return addPokemonFilters(spec, filter);
    }

    /**
     * Adds an {@code EXISTS (SELECT 1 FROM pokemon p WHERE p.species = root AND p.is_default_form
     * AND …)} clause to the species spec when the filter carries Pokémon-level criteria.
     * Backed by {@code idx_pokemon_species_default}.
     */
    private Specification<PokemonSpecies> addPokemonFilters(Specification<PokemonSpecies> spec, PokemonFilterDto filter) {
        if (!hasPokemonFilter(filter)) {
            return spec;
        }

        return spec.and((root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<Pokemon> pokemon = sub.from(Pokemon.class);
            sub.select(pokemon.get(FIELD_ID));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(pokemon.get(POKEMON_SPECIES), root));
            predicates.add(cb.isTrue(pokemon.get(POKEMON_IS_DEFAULT_FORM)));

            if (filter.getPrimaryTypeId() != null) {
                predicates.add(cb.equal(pokemon.get(POKEMON_PRIMARY_TYPE).get(FIELD_ID), filter.getPrimaryTypeId()));
            }
            if (filter.getSecondaryTypeId() != null) {
                predicates.add(cb.equal(pokemon.get(POKEMON_SECONDARY_TYPE).get(FIELD_ID), filter.getSecondaryTypeId()));
            }

            addRange(predicates, cb, pokemon.get(POKEMON_HEIGHT), filter.getMinHeight(), filter.getMaxHeight());
            addRange(predicates, cb, pokemon.get(POKEMON_WEIGHT), filter.getMinWeight(), filter.getMaxWeight());
            addRange(predicates, cb, pokemon.get(POKEMON_BASE_HP), filter.getMinBaseHp(), filter.getMaxBaseHp());
            addRange(predicates, cb, pokemon.get(POKEMON_BASE_ATK), filter.getMinBaseAtk(), filter.getMaxBaseAtk());
            addRange(predicates, cb, pokemon.get(POKEMON_BASE_DEF), filter.getMinBaseDef(), filter.getMaxBaseDef());
            addRange(predicates, cb, pokemon.get(POKEMON_BASE_SP_ATK), filter.getMinBaseSpAtk(), filter.getMaxBaseSpAtk());
            addRange(predicates, cb, pokemon.get(POKEMON_BASE_SP_DEF), filter.getMinBaseSpDef(), filter.getMaxBaseSpDef());
            addRange(predicates, cb, pokemon.get(POKEMON_BASE_SPEED), filter.getMinBaseSpeed(), filter.getMaxBaseSpeed());

            sub.where(predicates.toArray(new Predicate[0]));

            return cb.exists(sub);
        });
    }

    private static boolean hasPokemonFilter(PokemonFilterDto filter) {
        return filter.getPrimaryTypeId() != null
                || filter.getSecondaryTypeId() != null
                || filter.getMinHeight() != null
                || filter.getMaxHeight() != null
                || filter.getMinWeight() != null
                || filter.getMaxWeight() != null
                || filter.getMinBaseHp() != null
                || filter.getMaxBaseHp() != null
                || filter.getMinBaseAtk() != null
                || filter.getMaxBaseAtk() != null
                || filter.getMinBaseDef() != null
                || filter.getMaxBaseDef() != null
                || filter.getMinBaseSpAtk() != null
                || filter.getMaxBaseSpAtk() != null
                || filter.getMinBaseSpDef() != null
                || filter.getMaxBaseSpDef() != null
                || filter.getMinBaseSpeed() != null
                || filter.getMaxBaseSpeed() != null;
    }

    private static void addRange(List<Predicate> predicates, CriteriaBuilder cb, Path<? extends Number> path, Integer min, Integer max) {
        if (min == null && max == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Path<Integer> intPath = (Path<Integer>) path;

        if (min != null) {
            predicates.add(cb.greaterThanOrEqualTo(intPath, min));
        }
        if (max != null) {
            predicates.add(cb.lessThanOrEqualTo(intPath, max));
        }
    }
}
