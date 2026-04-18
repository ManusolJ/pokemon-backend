package com.poketeambuilder.dtos.front.species;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class PokemonSpeciesFilterDto implements FilterDtoInterface {

    private Long id;

    private String name;

    private String nameExact;

    private Integer generation;

    private Integer nationalDexNumber;

    private Boolean isBaby;

    private Boolean isMythical;

    private Boolean isLegendary;
    
    private Boolean isGenderless;

    private String growthRate;

    private Integer minCatchRate;
    private Integer maxCatchRate;

    private Integer minBaseHappiness;
    private Integer maxBaseHappiness;

    private Integer minGenderRate;
    private Integer maxGenderRate;

    private String evolutionItem;
    private String evolutionTrigger;
    private Boolean evolvesWithItem;
    private String evolutionTimeOfDay;
    private Integer minEvolutionLevel;
    private Integer maxEvolutionLevel;
    private Boolean evolvesWithLevelUp;
    private Boolean evolvesWithHappiness;
    private Boolean hasPreviousEvolution;

    private Integer eggGroupId;
    private Integer primaryTypeId;
}
