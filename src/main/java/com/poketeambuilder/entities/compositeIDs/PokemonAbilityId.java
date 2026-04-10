package com.poketeambuilder.entities.compositeIDs;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PokemonAbilityId implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "pokemon_id", nullable = false)
    private Integer pokemonId;

    @Column(name = "ability_id", nullable = false)
    private Integer abilityId;
}
