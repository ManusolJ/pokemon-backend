package com.poketeambuilder.dtos.front.pokemon;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class PokemonFilterDto implements FilterDtoInterface {

    // Pokemon-level fields
    private Integer id;

    private String name;
    private String nameExact;

    private Integer primaryTypeId;
    private Integer secondaryTypeId;

    private Integer minHeight;
    private Integer maxHeight;

    private Integer minWeight;
    private Integer maxWeight;

    private Boolean isDefaultForm;

    // Species-level fields (will require join in Specification)
    private Integer nationalDexNumber;
    private Integer generation;

    private Boolean isLegendary;
    private Boolean isMythical;
    private Boolean isBaby;

    private Boolean isGenderless;
    private Integer minGenderRate;
    private Integer maxGenderRate;

    private String growthRate;
    private String eggGroup;

    private Boolean hasPreviousEvolution;
    private Boolean evolvesWithLevelUp;
    private Boolean evolvesWithHappiness;
    private Boolean evolvesWithItem;

    private String evolutionTrigger;
    private String evolutionItem;
    private String evolutionTimeOfDay;
    private Integer minEvolutionLevel;
    private Integer maxEvolutionLevel;
}