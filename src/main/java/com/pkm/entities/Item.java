package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

/**
 * Represents a battle or utility item.
 */
@Entity
@Table(name = "items")
@Data
public class Item {

    /** Unique item identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    /** Item name (unique) */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /** Item effect description */
    @Column(name = "effect", nullable = false)
    @Lob
    private String effect;
}