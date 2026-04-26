package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.common.GenusEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextFallback;

import com.poketeambuilder.dtos.pokeapi.species.PokedexNumber;
import com.poketeambuilder.dtos.pokeapi.species.PokemonSpeciesApiDto;

import com.poketeambuilder.mappers.helpers.shared.FlavorTextSanitizer;

import org.springframework.stereotype.Component;

@Component
public class SpeciesIngestionHelper {

    public Integer extractNationalDex(PokemonSpeciesApiDto dto) {
        if (dto.pokedexNumbers() == null){
            return null;
        }
        
        return dto.pokedexNumbers().stream()
                .filter(pn -> pn.pokedex() != null && "national".equals(pn.pokedex().name()))
                .map(PokedexNumber::entryNumber)
                .findFirst()
                .orElse(null);
    }

    public Integer extractGeneration(PokeApiResource generation) {
        return generation == null ? null : generation.extractId();
    }

    public String extractGrowthRate(PokeApiResource growthRate) {
        return growthRate == null ? null : growthRate.name();
    }

    public String extractGenus(PokemonSpeciesApiDto dto) {
        return LocalizedEntries.english(dto.genera())
                .map(GenusEntry::genus)
                .orElse(null);
    }

    public String extractFlavorText(PokemonSpeciesApiDto dto) {
        return FlavorTextFallback.pickBestForSpecies(dto.flavorTextEntries())
                .map(FlavorTextEntry::flavorText)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }

    public String extractEggGroup1(PokemonSpeciesApiDto dto) {
        if (dto.eggGroups() == null || dto.eggGroups().isEmpty()) {
            return null;
        }

        return dto.eggGroups().get(0).name();
    }

    public String extractEggGroup2(PokemonSpeciesApiDto dto) {
        if (dto.eggGroups() == null || dto.eggGroups().size() < 2) {
            return null;
        }

        return dto.eggGroups().get(1).name();
    }
}
