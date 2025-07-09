package com.pkm.entities.keys;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Embeddable
@Data
@EqualsAndHashCode
public class TeamMemberId implements Serializable {
    
    private Long teamId;

    private Long userPokemonId;
}
