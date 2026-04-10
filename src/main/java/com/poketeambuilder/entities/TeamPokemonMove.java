package com.poketeambuilder.entities;

import com.poketeambuilder.entities.compositeIDs.TeamPokemonMoveId;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
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
@Table(name = "team_pokemon_move")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamPokemonMove {

    @EmbeddedId
    @Builder.Default
    @EqualsAndHashCode.Include
    private TeamPokemonMoveId id = new TeamPokemonMoveId();

    @MapsId("teamPokemonId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_pokemon_id", nullable = false)
    private TeamPokemon teamPokemon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "move_id", nullable = false)
    private Move move;
}
