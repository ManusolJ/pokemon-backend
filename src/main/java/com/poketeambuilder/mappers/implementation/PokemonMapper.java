package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Named;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.Pokemon;

import com.poketeambuilder.dtos.front.pokemon.form.PokemonReadDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonSummaryDto;

import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.shared.PokemonOrderNormalizer;
import com.poketeambuilder.mappers.helpers.resource.PokemonIngestionHelper;

/**
 * Maps {@link Pokemon} (form-level) between persistence and the front-end / PokeAPI DTOs.
 * The {@code species}, {@code primaryType}, and {@code secondaryType} references are
 * intentionally ignored on {@code toEntity}.
 */
@Mapper(
    config = MapperConfiguration.class,
    uses = { PokemonIngestionHelper.class, PokemonOrderNormalizer.class, TypeMapper.class, SpeciesMapper.class }
)
public interface PokemonMapper extends ReadMapper<Pokemon, PokemonReadDto>, ApiMapper<Pokemon, PokemonApiDto>, SummaryMapper<Pokemon, PokemonSummaryDto> {

    @Override
    @Mapping(target = "abilities", ignore = true)
    @Mapping(target = "heightInMeters", source = "height", qualifiedByName = "convertHeight")
    @Mapping(target = "weightInKilograms", source = "weight", qualifiedByName = "convertWeight")
    PokemonReadDto toReadDto(Pokemon entity);

    @Override
    PokemonSummaryDto toSummaryDto(Pokemon entity);

    @Override
    @Mapping(target = "species", ignore = true)
    @Mapping(target = "primaryType", ignore = true)
    @Mapping(target = "secondaryType", ignore = true)
    @Mapping(target = "isDefaultForm", source = "isDefault")
    @Mapping(target = "sortOrder", source = "order", qualifiedByName = "normalizePokemonOrder")
    @Mapping(target = "baseHp", source = "stats", qualifiedByName = "extractBaseHp")
    @Mapping(target = "baseAtk", source = "stats", qualifiedByName = "extractBaseAtk")
    @Mapping(target = "baseDef", source = "stats", qualifiedByName = "extractBaseDef")
    @Mapping(target = "baseSpAtk", source = "stats", qualifiedByName = "extractBaseSpAtk")
    @Mapping(target = "baseSpDef", source = "stats", qualifiedByName = "extractBaseSpDef")
    @Mapping(target = "baseSpeed", source = "stats", qualifiedByName = "extractBaseSpeed")
    @Mapping(target = "artworkUrl", source = "sprites", qualifiedByName = "extractArtworkUrl")
    @Mapping(target = "artworkShiny", source = "sprites", qualifiedByName = "extractArtworkShiny")
    @Mapping(target = "spriteShiny", source = "sprites", qualifiedByName = "extractSpriteShiny")
    @Mapping(target = "spriteDefault", source = "sprites", qualifiedByName = "extractSpriteDefault")
    Pokemon toEntity(PokemonApiDto dto);

    /** Converts PokeAPI height (decimetres) to metres. */
    @Named("convertHeight")
    default Double convertHeight(Integer heightInDecimeters) {
        return heightInDecimeters == null ? null : heightInDecimeters / 10.0;
    }

    /** Converts PokeAPI weight (hectograms) to kilograms. */
    @Named("convertWeight")
    default Double convertWeight(Integer weightInHectograms) {
        return weightInHectograms == null ? null : weightInHectograms / 10.0;
    }
}
