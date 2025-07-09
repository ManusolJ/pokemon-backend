package com.pkm.entities;

import jakarta.persistence.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.Data;

import com.pkm.entities.keys.TeamMemberId;

@Entity
@Table(name = "team_members")
@Data
public class TeamMember {
    
    @EmbeddedId
    private TeamMemberId id;

    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @MapsId("userPokemonId")
    @JoinColumn(name = "user_pokemon_id")
    private UserPokemon pokemon;

    @Column(name = "position")
    @Min(1) @Max(6)
    private int position;
}
