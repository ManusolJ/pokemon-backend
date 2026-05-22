package com.poketeambuilder.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Pokémon ability (Levitate, Intimidate, etc). {@link #effect} is the mechanical rule,
 * {@link #shortEffect} a one-line summary, and {@link #flavorText} the in-game text.
 */
@Entity
@Getter
@Setter
@Builder
@Table(name = "ability")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ability {
    
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "effect", columnDefinition = "TEXT")
    private String effect;

    @Column(name = "short_effect", columnDefinition = "TEXT")
    private String shortEffect;
    
    @Column(name = "flavor_text", columnDefinition = "TEXT")
    private String flavorText;
}
