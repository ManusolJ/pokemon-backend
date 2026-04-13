package com.poketeambuilder.entities;

import com.poketeambuilder.entities.compositeIDs.PokemonSpeciesEggGroupId;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.EmbeddedId;

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
@Table(name = "pokemon_species_egg_group")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PokemonSpeciesEggGroup {

    @EmbeddedId
    @Builder.Default
    @EqualsAndHashCode.Include
    private PokemonSpeciesEggGroupId id = new PokemonSpeciesEggGroupId();

    @MapsId("speciesId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id")
    private PokemonSpecies species;

    @MapsId("eggGroupId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "egg_group_id")
    private EggGroup eggGroup;
}