package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Pokémon species with base attributes.
 */
@Entity
@Table(name = "pokemon")
@Data
public class Pokemon {

    /** Pokédex number (unique identifier) */
    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    /** Species name (unique) */
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    /** Primary elemental type */
    @JoinColumn(name = "type1_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Type type1;

    /** Secondary elemental type (optional) */
    @JoinColumn(name = "type2_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Type type2 = null;

    /** Height in meters */
    @Column(name = "height")
    private BigDecimal height;

    /** Weight in kilograms */
    @Column(name = "weight")
    private BigDecimal weight;

    /** Species description */
    @Column(name = "description")
    @Lob
    private String description;

    // Base stats
    @Column(name = "hp", nullable = false)
    private int hp;
    @Column(name = "attack", nullable = false)
    private int attack;
    @Column(name = "defense", nullable = false)
    private int defense;
    @Column(name = "special_attack", nullable = false)
    private int specialAttack;
    @Column(name = "special_defense", nullable = false)
    private int specialDefense;
    @Column(name = "speed", nullable = false)
    private int speed;

    /** Learnable abilities */
    @ManyToMany
    @JoinTable(
        name = "pokemon_abilities",
        joinColumns = @JoinColumn(name = "pokemon_id"),
        inverseJoinColumns = @JoinColumn(name = "ability_id")
    )
    private Set<Ability> abilities = new HashSet<>();

    /** Learnable moves */
    @ManyToMany
    @JoinTable(
        name = "pokemon_moves",
        joinColumns = @JoinColumn(name = "pokemon_id"),
        inverseJoinColumns = @JoinColumn(name = "move_id")
    )
    private Set<Move> moves = new HashSet<>();

    @PrePersist
    private void validateStats() {
        if (hp < 0 || attack < 0 || defense < 0 || specialAttack < 0 || specialDefense < 0 || speed < 0) {
            throw new IllegalStateException("Base stats cannot be negative");
        }
    }
}