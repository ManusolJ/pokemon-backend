package com.poketeambuilder.entities;

import com.poketeambuilder.entities.compositeIDs.TypeEffectivenessId;

import java.math.BigDecimal;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.EmbeddedId;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Damage multiplier applied when {@link #attackingType} hits {@link #defendingType}. One row
 * per ordered pair; the canonical 18-type matrix yields 324 rows. Multiplier is one of
 * {@code 0.00}, {@code 0.50}, {@code 1.00}, or {@code 2.00} in the standard chart.
 */
@Entity
@Getter
@Setter
@Builder
@Table(name = "type_effectiveness")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TypeEffectiveness {
    
    @EmbeddedId
    @Builder.Default
    @EqualsAndHashCode.Include
    private TypeEffectivenessId id = new TypeEffectivenessId();

    @MapsId("attackingTypeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attacking_type_id", nullable = false)
    private Type attackingType;

    @MapsId("defendingTypeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defending_type_id", nullable = false)
    private Type defendingType;

    @NotNull
    @Column(name = "multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal multiplier;
}