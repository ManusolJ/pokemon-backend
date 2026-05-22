package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Named;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.ArrayList;

import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesReadDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesSummaryDto;

import com.poketeambuilder.dtos.pokeapi.species.PokemonSpeciesApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.shared.TextExtractor;
import com.poketeambuilder.mappers.helpers.shared.PokemonOrderNormalizer;
import com.poketeambuilder.mappers.helpers.resource.SpeciesIngestionHelper;

/**
 * Maps {@link PokemonSpecies} between persistence and the front-end / PokeAPI DTOs. The summary
 * mapping is hand-written because the default-form-derived fields ({@code primaryType},
 * {@code secondaryType}, {@code spriteDefault}) are enriched later by the service layer.
 */
@Mapper(
    config = MapperConfiguration.class,
    uses = { SpeciesIngestionHelper.class, TextExtractor.class, PokemonOrderNormalizer.class }
)
public interface SpeciesMapper extends ReadMapper<PokemonSpecies, PokemonSpeciesReadDto>, ApiMapper<PokemonSpecies, PokemonSpeciesApiDto>, SummaryMapper<PokemonSpecies, PokemonSpeciesSummaryDto> {

    @Override
    @Mapping(target = "eggGroups", source = ".", qualifiedByName = "combineEggGroups")
    PokemonSpeciesReadDto toReadDto(PokemonSpecies entity);

    @Override
    default PokemonSpeciesSummaryDto toSummaryDto(PokemonSpecies entity) {
        if (entity == null) {
            return null;
        }

        return new PokemonSpeciesSummaryDto(
            entity.getId(),
            entity.getName(),
            entity.getGenus(),
            entity.getNationalDexNumber(),
            entity.getSortOrder(),
            entity.getGenderRate(),
            null,
            null,
            null
        );
    }

    @Override
    @Mapping(target = "catchRate", source = "captureRate")
    @Mapping(target = "genus", source = "genera", qualifiedByName = "extractGenus")
    @Mapping(target = "sortOrder", source = "order", qualifiedByName = "normalizePokemonOrder")
    @Mapping(target = "eggGroup1", source = "eggGroups", qualifiedByName = "extractEggGroup1")
    @Mapping(target = "eggGroup2", source = "eggGroups", qualifiedByName = "extractEggGroup2")
    @Mapping(target = "generation", source = "generation", qualifiedByName = "extractGeneration")
    @Mapping(target = "growthRate", source = "growthRate", qualifiedByName = "extractGrowthRate")
    @Mapping(target = "nationalDexNumber", source = "pokedexNumbers", qualifiedByName = "extractNationalDex")
    @Mapping(target = "flavorText", source = "flavorTextEntries", qualifiedByName = "extractSpeciesFlavorText")
    PokemonSpecies toEntity(PokemonSpeciesApiDto apiDto);

    /** Flattens the two egg-group columns into the list shape the front-end expects. */
    @Named("combineEggGroups")
    default List<String> combineEggGroups(PokemonSpecies entity) {
        List<String> groups = new ArrayList<>();

        if (entity.getEggGroup1() != null) {
            groups.add(entity.getEggGroup1());
        }
        if (entity.getEggGroup2() != null) {
            groups.add(entity.getEggGroup2());
        }

        return groups;
    }
}
