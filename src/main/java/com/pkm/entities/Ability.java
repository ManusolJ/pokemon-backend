package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

/**
 * Represents a Pokémon ability with battle effects.
 */
@Entity
@Table(name = "abilities")
@Data
public class Ability {

    /** Unique ability identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    /** Ability name (unique) */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /** Detailed effect description */
    @Column(name = "effect", nullable = false)
    @Lob
    private String effect;
}