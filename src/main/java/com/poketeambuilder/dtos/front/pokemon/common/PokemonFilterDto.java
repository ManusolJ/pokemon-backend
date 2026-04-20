package com.poketeambuilder.dtos.front.pokemon.common;

import java.util.List;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class PokemonFilterDto implements FilterDtoInterface {

    private Integer id;

    private String name;
    private String nameExact;

    private Integer primaryTypeId;
    private Integer secondaryTypeId;

    private Integer minHeight;
    private Integer maxHeight;

    private Integer minWeight;
    private Integer maxWeight;

    private Integer minBaseHp;
    private Integer maxBaseHp;
    private Integer minBaseAtk;
    private Integer maxBaseAtk;
    private Integer minBaseDef;
    private Integer maxBaseDef;
    private Integer minBaseSpAtk;
    private Integer maxBaseSpAtk;
    private Integer minBaseSpDef;
    private Integer maxBaseSpDef;
    private Integer minBaseSpeed;
    private Integer maxBaseSpeed;

    private Boolean isDefaultForm;

    private Integer generation;
    private Integer nationalDexNumber;

    private Boolean isBaby;
    private Boolean isMythical;
    private Boolean isLegendary;

    private Boolean isGenderless;
    private Integer minGenderRate;
    private Integer maxGenderRate;

    private Integer minBaseHappiness;
    private Integer maxBaseHappiness;

    private String growthRate;
    private List<String> eggGroup;

    private Boolean hasPreviousEvolution;

    private Boolean evolvesWithItem;
    private Boolean evolvesWithLevelUp;
    private Boolean evolvesWithHappiness;

    private String evolutionItem;
    private String evolutionTrigger;
    private String evolutionTimeOfDay;
    
    private Integer minEvolutionLevel;
    private Integer maxEvolutionLevel;
}