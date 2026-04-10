package com.poketeambuilder.entities;

import com.poketeambuilder.entities.compositeIDs.PokemonAbilityId;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;

import jakarta.validation.constraints.NotNull;

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
@Table(name = "pokemon_ability")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PokemonAbility {
    
    @EmbeddedId
    @Builder.Default
    @EqualsAndHashCode.Include
    private PokemonAbilityId id = new PokemonAbilityId();

    @MapsId("pokemonId")
    @JoinColumn(name = "pokemon_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Pokemon pokemon;

    @MapsId("abilityId")
    @JoinColumn(name = "ability_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Ability ability;

    @NotNull
    @Column(name = "slot", nullable = false)
    private Integer slot;
    
    @NotNull
    @Builder.Default
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;
}
