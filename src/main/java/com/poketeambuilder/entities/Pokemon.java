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
@Table(name = "pokemon")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pokemon {
    
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private PokemonSpecies species;

    @NotNull
    @Builder.Default
    @Column(name = "is_default_form", nullable = false)
    private Boolean isDefaultForm = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_type_id", nullable = false)
    private Type primaryType;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_type_id")
    private Type secondaryType = null;

    @NotNull
    @Column(name = "base_hp", nullable = false)
    private Integer baseHp;

    @NotNull
    @Column(name = "base_atk", nullable = false)
    private Integer baseAtk;

    @NotNull
    @Column(name = "base_def", nullable = false)
    private Integer baseDef;

    @NotNull
    @Column(name = "base_sp_atk", nullable = false)
    private Integer baseSpAtk;

    @NotNull
    @Column(name = "base_sp_def", nullable = false)
    private Integer baseSpDef;

    @NotNull
    @Column(name = "base_speed", nullable = false)
    private Integer baseSpeed;

    @Column(name = "height")
    private Integer height;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "artwork_url", columnDefinition = "TEXT")
    private String artworkUrl;
    
    @Column(name = "sprite_shiny", columnDefinition = "TEXT")
    private String spriteShiny;

    @Column(name = "sprite_default", columnDefinition = "TEXT")
    private String spriteDefault;
}