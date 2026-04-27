package com.poketeambuilder.services.query;

import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.validation.annotation.Validated;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.poketeambuilder.entities.Pokemon;
import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.dtos.front.pokemon.common.PokemonFilterDto;
import com.poketeambuilder.dtos.front.pokemon.individual.PokemonReadDto;
import com.poketeambuilder.dtos.front.pokemon.individual.PokemonSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.PokemonMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.PokemonRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.CriteriaQuery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class PokemonQueryService extends AbstractQueryService<Pokemon, Integer, PokemonFilterDto, PokemonReadDto> {

    private final PokemonMapper pokemonMapper;

    private final PokemonRepository pokemonRepository;

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRIMARY_TYPE_ID = "primaryType.id";
    private static final String FIELD_SECONDARY_TYPE_ID = "secondaryType.id";
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

    private static final int GENDERLESS_RATE = -1;

    @Override
    protected String getEntityName() {
        return "Pokemon";
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

        Specification<Pokemon> spec = builder.build();

        spec = addSpeciesFilters(spec, filter);

        return spec;
    }

    private Specification<Pokemon> addSpeciesFilters(Specification<Pokemon> spec, PokemonFilterDto filter) {
        boolean hasSpeciesFilter = filter.getGeneration() != null
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

        if (!hasSpeciesFilter) {
            return spec;
        }

        return spec.and((root, query, cb) -> {
            Join<Pokemon, PokemonSpecies> species = root.join(JOIN_SPECIES);
            
            var predicates = cb.conjunction();

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

            if (Boolean.TRUE.equals(filter.getIsGenderless())) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_GENDER_RATE), GENDERLESS_RATE));
            } else if (Boolean.FALSE.equals(filter.getIsGenderless())) {
                predicates = cb.and(predicates, cb.notEqual(species.get(SPECIES_GENDER_RATE), GENDERLESS_RATE));
            }
            if (filter.getMinGenderRate() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(species.get(SPECIES_GENDER_RATE), filter.getMinGenderRate()));
            }
            if (filter.getMaxGenderRate() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(species.get(SPECIES_GENDER_RATE), filter.getMaxGenderRate()));
            }

            if (filter.getGrowthRate() != null && !filter.getGrowthRate().isBlank()) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_GROWTH_RATE), filter.getGrowthRate()));
            }
            if (filter.getMinBaseHappiness() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(species.get(SPECIES_BASE_HAPPINESS), filter.getMinBaseHappiness()));
            }
            if (filter.getMaxBaseHappiness() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(species.get(SPECIES_BASE_HAPPINESS), filter.getMaxBaseHappiness()));
            }

            List<String> eggGroups = filter.getEggGroups();

            if (eggGroups != null && !eggGroups.isEmpty()) {
                predicates = cb.and(predicates, cb.or(
                        species.get(SPECIES_EGG_GROUP_1).in(eggGroups),
                        species.get(SPECIES_EGG_GROUP_2).in(eggGroups)
                ));
            }

            if (filter.getHasPreviousEvolution() != null && filter.getHasPreviousEvolution()) {
                predicates = cb.and(predicates, cb.isNotNull(species.get(SPECIES_PREVIOUS_EVOLUTION)));
            } else if (filter.getHasPreviousEvolution() != null && !filter.getHasPreviousEvolution()) {
                predicates = cb.and(predicates, cb.isNull(species.get(SPECIES_PREVIOUS_EVOLUTION)));
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

            if (filter.getEvolvesWithItem() != null && filter.getEvolvesWithItem()) {
                predicates = cb.and(predicates, cb.isNotNull(species.get(SPECIES_EVOLUTION_ITEM)));
            }

            if (filter.getEvolvesWithLevelUp() != null && filter.getEvolvesWithLevelUp()) {
                predicates = cb.and(predicates, cb.isNotNull(species.get(SPECIES_EVOLUTION_MIN_LEVEL)));
            }

            if (filter.getEvolvesWithHappiness() != null && filter.getEvolvesWithHappiness()) {
                predicates = cb.and(predicates, cb.equal(species.get(SPECIES_EVOLUTION_TRIGGER), "happiness"));
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

    private void addRange(SpecificationBuilder<Pokemon> builder, String field, Integer min, Integer max) {
        if (min != null) {
            builder.with(field, min, SearchOperation.GREATER_THAN_OR_EQUAL);
        }

        if (max != null) {
            builder.with(field, max, SearchOperation.LESS_THAN_OR_EQUAL);
        }
    }
}