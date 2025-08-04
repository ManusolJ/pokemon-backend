package com.pkm.entities;

import jakarta.persistence.*;

import jakarta.validation.constraints.Positive;

import lombok.Data;

import com.pkm.utils.enums.MoveCategory;

/**
 * Represents a Pokémon move with battle properties and effects.
 */
@Entity
@Table(name = "moves")
@Data
public class Move {

    /** Unique identifier for the move */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, insertable = false, updatable = false)
    private Long id;

    /** Name of the move (unique) */
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    /** Detailed effect description */
    @Column(name = "effect", nullable = false)
    @Lob
    private String effect;

    /** Elemental type of the move */
    @JoinColumn(name = "type_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Positive
    private Type type;

    /** Base damage power (0 or null for status moves) */
    @Column(name = "power")
    private Integer power;

    /** Hit accuracy percentage (0 or null for infalible moves) */
    @Column(name = "accuracy")
    private Integer accuracy;

    /** Power Points (usage limit) */
    @Column(name = "pp", nullable = false)
    private int pp;

    /** Turn execution priority */
    @Column(name = "priority")
    private int priority = 0;

    /** Physical/Special/Status category */
    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private MoveCategory category = MoveCategory.STATUS;
}