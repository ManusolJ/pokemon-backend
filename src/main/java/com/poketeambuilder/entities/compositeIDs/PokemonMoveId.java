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

/**
 * Composite identifier for {@link com.poketeambuilder.entities.PokemonMove}. Field order
 * mirrors the database primary key {@code (pokemon_id, move_id, learn_method)} so generated
 * cache keys and hash codes line up with the underlying index.
 */
@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PokemonMoveId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "pokemon_id", nullable = false)
    private Integer pokemonId;

    @Column(name = "move_id", nullable = false)
    private Integer moveId;

    @Size(max = 50)
    @Column(name = "learn_method", nullable = false, length = 50)
    private String learnMethod;
}
