package com.poketeambuilder.dtos.front.pokemon.species;

import java.util.List;

public record PokemonSpeciesReadDto(
    int id,
    String name,
    String genus,
    Integer order,
    Integer hatchCounter,
    Integer nationalDexNumber,
    String flavorText,
    Integer catchRate,
    String growthRate,
    Integer genderRate,
    Integer generation,
    Integer baseHappiness,
    Boolean isBaby,
    Boolean isMythical,
    Boolean isLegendary,
    List<String> eggGroups,
    PokemonSpeciesSummaryDto previousEvolution,
    String evolutionItem,
    String evolutionTrigger,
    String evolutionHeldItem,
    String evolutionTimeOfDay,
    Integer evolutionMinLevel,
    Integer evolutionMinHappiness
) {
}
