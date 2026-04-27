package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.common.GenusEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;

import com.poketeambuilder.dtos.pokeapi.species.PokedexNumber;

import java.util.List;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

@Component
public class SpeciesIngestionHelper {

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

    @Named("extractGeneration")
    public Integer extractGeneration(PokeApiResource generation) {
        return generation == null ? null : generation.extractId();
    }

    @Named("extractGrowthRate")
    public String extractGrowthRate(PokeApiResource growthRate) {
        return growthRate == null ? null : growthRate.name();
    }

    @Named("extractGenus")
    public String extractGenus(List<GenusEntry> genera) {
        return LocalizedEntries.english(genera)
                .map(GenusEntry::genus)
                .orElse(null);
    }

    @Named("extractEggGroup1")
    public String extractEggGroup1(List<PokeApiResource> eggGroups) {
        if (eggGroups == null || eggGroups.isEmpty()) {
            return null;
        }

        return eggGroups.get(0).name();
    }

    @Named("extractEggGroup2")
    public String extractEggGroup2(List<PokeApiResource> eggGroups) {
        if (eggGroups == null || eggGroups.size() < 2) {
            return null;
        }

        return eggGroups.get(1).name();
    }
}
