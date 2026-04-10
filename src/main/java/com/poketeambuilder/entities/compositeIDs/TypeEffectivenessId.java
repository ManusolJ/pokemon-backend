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
public class TypeEffectivenessId implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "attacking_type_id", nullable = false)
    private Integer attackingType;

    @Column(name = "defending_type_id", nullable = false)
    private Integer defendingType;   
}
