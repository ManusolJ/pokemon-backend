package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

import com.pkm.utils.enums.Stat;

/**
 * Represents a Pokémon nature that influences stat growth.
 */
@Entity
@Table(name = "natures")
@Data
public class Nature {

    /** Unique identifier for the nature */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    /** Name of the nature (unique) */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /** Stat increased by this nature */
    @Column(name = "increased_stat", nullable = false)
    private Stat increasedStat;

    /** Stat decreased by this nature */
    @Column(name = "decreased_stat", nullable = false)
    private Stat decreasedStat;

    /** Validates that increased/decreased stats differ */
    @PrePersist
    @PreUpdate
    private void validateStats() {
        if (increasedStat != null && increasedStat.equals(decreasedStat)) {
            throw new IllegalStateException("Increased and decreased stats cannot be the same");
        }
    }
}