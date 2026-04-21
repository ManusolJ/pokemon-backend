package com.poketeambuilder.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@Builder
@Table(name = "pokemon_species")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PokemonSpecies {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer order;

    @NotBlank
    @Size(max = 50)
    @Column(name = "genus", nullable = false, length = 50)
    private String genus;

    @NotNull
    @Column(name = "national_dex_number", nullable = false)
    private Integer nationalDexNumber;

    @Column(name = "gender_rate")
    private Integer genderRate;

    @Column(name = "flavor_text", columnDefinition = "TEXT")
    private String flavorText;

    @Column(name = "generation")
    private Integer generation;

    @Column(name = "catch_rate")
    private Integer catchRate;

    @Column(name = "hatch_counter")
    private Integer hatchCounter;

    @Column(name = "base_happiness")
    private Integer baseHappiness;

    @Size(max = 30)
    @Column(name = "growth_rate", length = 30)
    private String growthRate;

    @Builder.Default
    @Column(name = "is_baby", nullable = false)
    private Boolean isBaby = false;

    @Builder.Default
    @Column(name = "is_mythical", nullable = false)
    private Boolean isMythical = false;

    @Builder.Default
    @Column(name = "is_legendary", nullable = false)
    private Boolean isLegendary = false;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_evolution_id")
    private PokemonSpecies previousEvolution = null;

    @Size(max = 30)
    @Column(name = "egg_group_1", length = 30)
    private String eggGroup1;
 
    @Size(max = 30)
    @Column(name = "egg_group_2", length = 30)
    private String eggGroup2;

    @Size(max = 30)
    @Column(name = "evolution_trigger", length = 30)
    private String evolutionTrigger;

    @Column(name = "evolution_min_level")
    private Integer evolutionMinLevel;

    @Size(max = 50)
    @Column(name = "evolution_item", length = 50)
    private String evolutionItem;

    @Size(max = 50)
    @Column(name = "evolution_held_item", length = 50)
    private String evolutionHeldItem;

    @Column(name = "evolution_min_happiness")
    private Integer evolutionMinHappiness;

    @Size(max = 10)
    @Column(name = "evolution_time_of_day", length = 10)
    private String evolutionTimeOfDay;
}