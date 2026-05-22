package com.poketeambuilder.mappers.helpers.resource;

import java.util.List;

import com.poketeambuilder.utils.pokeapi.LocalizedEntries;

import com.poketeambuilder.dtos.pokeapi.common.GenusEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.dtos.pokeapi.species.PokedexNumber;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

/**
 * MapStruct helper bean that resolves species-level fields from the PokeAPI payload —
 * national dex number, generation, growth rate, genus, and the two egg-group slots.
 */
@Component
public class SpeciesIngestionHelper {

    /** Returns the national-Pokédex entry number, or {@code null} when the species has no national listing. */
    @Named("extractNationalDex")
    public Integer extractNationalDex(List<PokedexNumber> pokedexNumbers) {
        if (pokedexNumbers == null || pokedexNumbers.isEmpty()) {
            return null;
        }

        return pokedexNumbers.stream()
                .filter(pn -> pn.pokedex() != null && "national".equals(pn.pokedex().name()))
                .map(PokedexNumber::entryNumber)
                .findFirst()
                .orElse(null);
    }

    /** Parses the trailing id from the generation resource URL ({@code /generation/9/} → {@code 9}). */
    @Named("extractGeneration")
    public Integer extractGeneration(PokeApiResource generation) {
        return generation == null ? null : generation.extractId();
    }

    /** Returns the growth-rate slug (e.g. {@code "medium-slow"}). */
    @Named("extractGrowthRate")
    public String extractGrowthRate(PokeApiResource growthRate) {
        return growthRate == null ? null : growthRate.name();
    }

    /** Returns the English genus string (e.g. {@code "Seed Pokémon"}). */
    @Named("extractGenus")
    public String extractGenus(List<GenusEntry> genera) {
        return LocalizedEntries.english(genera)
                .map(GenusEntry::genus)
                .orElse(null);
    }

    /** Returns the first egg group, or {@code null} when the species has none. */
    @Named("extractEggGroup1")
    public String extractEggGroup1(List<PokeApiResource> eggGroups) {
        if (eggGroups == null || eggGroups.isEmpty()) {
            return null;
        }

        return eggGroups.get(0).name();
    }

    /** Returns the second egg group, or {@code null} when the species has fewer than two. */
    @Named("extractEggGroup2")
    public String extractEggGroup2(List<PokeApiResource> eggGroups) {
        if (eggGroups == null || eggGroups.size() < 2) {
            return null;
        }

        return eggGroups.get(1).name();
    }
}
