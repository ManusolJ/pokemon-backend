package com.poketeambuilder.services.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import org.springframework.validation.annotation.Validated;

import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.dtos.front.pokemon.common.PokemonFilterDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesReadDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.SpeciesMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.SpeciesRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.CriteriaQuery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class SpeciesQueryService extends AbstractQueryService<PokemonSpecies, Integer, PokemonFilterDto, PokemonSpeciesReadDto> {

    private final SpeciesMapper speciesMapper;
    private final SpeciesRepository speciesRepository;

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
    private static final String FIELD_PREVIOUS_EVOLUTION = "previousEvolution";
    private static final String FIELD_EVOLUTION_TRIGGER = "evolutionTrigger";
    private static final String FIELD_EVOLUTION_ITEM = "evolutionItem";
    private static final String FIELD_EVOLUTION_TIME_OF_DAY = "evolutionTimeOfDay";
    private static final String FIELD_EVOLUTION_MIN_LEVEL = "evolutionMinLevel";

    private static final int GENDERLESS_RATE = -1;

    @Override
    protected String getEntityName() {
        return "Species";
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

    public Page<PokemonSpeciesSummaryDto> filterSummaries(@Valid @NotNull PokemonFilterDto filter, @NotNull Pageable pageable) {
        return filterAndMap(filter, pageable, speciesMapper::toSummaryDto);
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
        } else if (filter.getIsGenderless() != null && !filter.getIsGenderless()) {
            spec = spec.and((root, query, cb) -> cb.notEqual(root.get(FIELD_GENDER_RATE), GENDERLESS_RATE));
        }

        if (filter.getHasPreviousEvolution() != null && filter.getHasPreviousEvolution()) {
            spec = spec.and((root, query, cb) -> cb.isNotNull(root.get(FIELD_PREVIOUS_EVOLUTION)));
        } else if (filter.getHasPreviousEvolution() != null && !filter.getHasPreviousEvolution()) {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get(FIELD_PREVIOUS_EVOLUTION)));
        }

        List<String> eggGroups = filter.getEggGroups();

        if (eggGroups != null && !eggGroups.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    root.get(FIELD_EGG_GROUP_1).in(eggGroups),
                    root.get(FIELD_EGG_GROUP_2).in(eggGroups)
            ));
        }

        return spec;
    }
}