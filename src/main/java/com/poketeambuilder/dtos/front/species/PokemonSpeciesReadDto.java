package com.poketeambuilder.dtos.front.species;

public record PokemonSpeciesReadDto(
    long id,
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
    String eggGroup1,
    String eggGroup2,
    PokemonSpeciesSummaryDto previousEvolution,
    String evolutionItem,
    String evolutionTrigger,
    String evolutionHeldItem,
    String evolutionTimeOfDay,
    Integer evolutionMinLevel,
    Integer evolutionMinHappiness
) {
}
