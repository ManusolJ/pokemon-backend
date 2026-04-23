package com.poketeambuilder.dtos.pokeapi.species;

import com.poketeambuilder.dtos.pokeapi.common.GenusEntry;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonSpeciesApiDto(
        Integer id,
        String name,
        Integer order,
        @JsonProperty("gender_rate")      Integer genderRate,
        @JsonProperty("capture_rate")     Integer captureRate,
        @JsonProperty("base_happiness")   Integer baseHappiness,
        @JsonProperty("hatch_counter")    Integer hatchCounter,
        @JsonProperty("is_baby")          Boolean isBaby,
        @JsonProperty("is_legendary")     Boolean isLegendary,
        @JsonProperty("is_mythical")      Boolean isMythical,
        @JsonProperty("growth_rate")      PokeApiResource growthRate,
        @JsonProperty("evolves_from_species") PokeApiResource evolvesFromSpecies,
        @JsonProperty("evolution_chain")  PokeApiResource evolutionChain,
        PokeApiResource generation,
        @JsonProperty("egg_groups")       List<PokeApiResource> eggGroups,
        @JsonProperty("pokedex_numbers")  List<PokedexNumber> pokedexNumbers,
        List<GenusEntry> genera,
        @JsonProperty("flavor_text_entries") List<FlavorTextEntry> flavorTextEntries
) {

}