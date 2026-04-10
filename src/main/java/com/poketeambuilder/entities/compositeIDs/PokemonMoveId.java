package com.poketeambuilder.entities.compositeIDs;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

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
public class PokemonMoveId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "move_id", nullable = false)
    private Integer moveId;

    @Column(name = "pokemon_id", nullable = false)
    private Integer pokemonId;

    @Size(max = 20)
    @Column(name = "learn_method", nullable = false, length = 20)
    private String learnMethod;
}
