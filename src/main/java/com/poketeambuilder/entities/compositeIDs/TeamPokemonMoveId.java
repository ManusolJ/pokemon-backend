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
public class TeamPokemonMoveId implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "team_pokemon_id", nullable = false)
    private Long teamPokemonId;

    @Column(name = "slot_position", nullable = false)
    private Integer slotPosition;
}
