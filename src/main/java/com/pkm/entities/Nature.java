package com.pkm.entities;

import com.pkm.utils.enums.Stat;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "natures")
@Data
public class Nature {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private int id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "increased_stat", nullable = false)
    private Stat increasedStat;

    @Column(name = "decreased_stat", nullable = false)
    private Stat decreasedStat;

    @PrePersist @PreUpdate
    private void validateStats() {
        if (increasedStat != null && increasedStat.equals(decreasedStat)) {
            throw new IllegalStateException("Increased and decreased stats cannot be the same");
        }
    }
}
