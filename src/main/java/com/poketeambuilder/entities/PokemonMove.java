package com.poketeambuilder.entities;

import com.poketeambuilder.entities.compositeIDs.PokemonMoveId;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
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
@Table(name = "pokemon_move")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PokemonMove {
    
    @EmbeddedId
    @Builder.Default
    @EqualsAndHashCode.Include
    private PokemonMoveId id = new PokemonMoveId();

    @MapsId("pokemonId")
    @JoinColumn(name = "pokemon_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Pokemon pokemon;

    @MapsId("moveId")
    @JoinColumn(name = "move_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Move move;

    @Column(name = "level_learned_at")
    private Integer levelLearnedAt;
}
