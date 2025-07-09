package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

/**
 * Represents a type from the pokemon elemental table.
 */
@Entity
@Table(name = "types")
@Data
public class Type {

    /** Unique type identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    /** Type name (e.g. Fire, Water) */
    @Column(length = 20, nullable = false)
    private String name;
}