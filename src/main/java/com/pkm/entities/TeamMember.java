package com.pkm.entities;

import jakarta.persistence.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.Data;

import com.pkm.entities.keys.TeamMemberId;

/**
 * Represents a Pokémon within a trainer's team.
 */
@Entity
@Table(name = "team_members")
@Data
public class TeamMember {

    /** Composite identifier (team + pokemon) */
    @EmbeddedId
    private TeamMemberId id;

    /** Associated team */
    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    /** Pokémon instance in this slot */
    @ManyToOne
    @MapsId("userPokemonId")
    @JoinColumn(name = "user_pokemon_id")
    private UserPokemon pokemon;

    /** Battle position (1-6) */
    @Column(name = "position")
    @Min(1)
    @Max(6)
    private int position;
}