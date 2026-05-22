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

/**
 * Composite identifier for {@link com.poketeambuilder.entities.TypeEffectiveness}. Mirrors
 * the database primary key {@code (attacking_type_id, defending_type_id)}.
 */
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
    private Integer attackingTypeId;

    @Column(name = "defending_type_id", nullable = false)
    private Integer defendingTypeId;
}
