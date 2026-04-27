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

    @MapsId("attackingType")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attacking_type_id")
    private Type attackingType;

    @MapsId("defendingType")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defending_type_id")
    private Type defendingType;

    @NotNull
    @Column(name = "multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal multiplier;
}