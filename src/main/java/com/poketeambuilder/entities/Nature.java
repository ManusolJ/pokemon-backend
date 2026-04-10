package com.poketeambuilder.entities;

import com.poketeambuilder.utils.enums.StatName;

import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

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
@Table(name = "nature")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nature {
    
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private String name;

    @Size(max = 20)
    @Builder.Default
    @Column(name = "increased_stat", length = 20)
    private StatName increasedStat = null;

    @Size(max = 20)
    @Builder.Default
    @Column(name = "decreased_stat", length = 20)
    private StatName decreasedStat = null;

    @PreUpdate
    @PrePersist
    private void validateStats() {
        boolean hasIncreased = increasedStat != null;
        boolean hasDecreased = decreasedStat != null;

        if (hasIncreased != hasDecreased) {
            throw new IllegalStateException("Both increased and decreased stats must be set, or both must be null");
        }

        if (hasIncreased && increasedStat.equals(decreasedStat)) {
            throw new IllegalStateException("Increased and decreased stats cannot be the same");
        }
    }
}

