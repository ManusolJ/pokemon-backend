package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Named;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesReadDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesSummaryDto;

import com.poketeambuilder.dtos.pokeapi.species.PokemonSpeciesApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.shared.TextExtractor;
import com.poketeambuilder.mappers.helpers.resource.SpeciesIngestionHelper;

import java.util.ArrayList;
import java.util.List;

@Mapper(config = MapperConfiguration.class, uses = { SpeciesIngestionHelper.class, TextExtractor.class })
public interface SpeciesMapper extends ReadMapper<PokemonSpecies, PokemonSpeciesReadDto>, ApiMapper<PokemonSpecies, PokemonSpeciesApiDto>, SummaryMapper<PokemonSpecies, PokemonSpeciesSummaryDto> {
    
    @Override
    @Mapping(target = "eggGroups", source = ".", qualifiedByName = "combineEggGroups")
    PokemonSpeciesReadDto toReadDto(PokemonSpecies entity);

    @Override
    PokemonSpeciesSummaryDto toSummaryDto(PokemonSpecies entity);

    @Override
    @Mapping(target = "catchRate", source = "captureRate")
    @Mapping(target = "genus", source = "genera", qualifiedByName = "extractGenus")
    @Mapping(target = "eggGroup1", source = "eggGroups", qualifiedByName = "extractEggGroup1")
    @Mapping(target = "eggGroup2", source = "eggGroups", qualifiedByName = "extractEggGroup2")
    @Mapping(target = "generation", source = "generation", qualifiedByName = "extractGeneration")
    @Mapping(target = "growthRate", source = "growthRate", qualifiedByName = "extractGrowthRate")
    @Mapping(target = "nationalDexNumber", source = "pokedexNumbers", qualifiedByName = "extractNationalDex")
    @Mapping(target = "flavorText", source = "flavorTextEntries", qualifiedByName = "extractSpeciesFlavorText")
    PokemonSpecies toEntity(PokemonSpeciesApiDto apiDto);

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