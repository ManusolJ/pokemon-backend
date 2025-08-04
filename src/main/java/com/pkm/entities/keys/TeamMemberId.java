package com.pkm.entities.keys;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

import lombok.Data;

import lombok.EqualsAndHashCode;

/**
 * Composite key for TeamMember (team + pokemon instance).
 */
@Embeddable
@Data
@EqualsAndHashCode
public class TeamMemberId implements Serializable {

    /** Team identifier */
    private Long teamId;

    /** UserPokemon instance identifier */
    private Long userPokemonId;
}