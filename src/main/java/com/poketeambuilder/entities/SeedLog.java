package com.poketeambuilder.entities;

import com.poketeambuilder.utils.enums.SeedStatus;

import java.time.Instant;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Operational record of a PokeAPI seed run. One row per invocation of the seed pipeline.
 * {@link #status} transitions from {@code RUNNING} to {@code COMPLETED} or {@code FAILED};
 * {@link #completedAt} is stamped automatically when either terminal state is reached.
 */
@Entity
@Getter
@Setter
@Builder
@Table(name = "seed_log")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Builder.Default
    @Column(name = "entries_added")
    private Integer entriesAdded = 0;

    @Builder.Default
    @Column(name = "errors")
    private Integer errors = 0;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private SeedStatus status = SeedStatus.RUNNING;

    @Column(name = "triggered_by")
    private String triggeredBy;

    /** Sets {@link #startedAt} to the current instant before the first insert. */
    @PrePersist
    protected void onCreate() {
        this.startedAt = Instant.now();
    }

    /**
     * Stamps {@link #completedAt} when the run reaches a terminal state. No-op for non-terminal
     * status updates so an intermediate save doesn't prematurely close the run.
     */
    @PreUpdate
    protected void onUpdate() {
        if (this.status == SeedStatus.COMPLETED || this.status == SeedStatus.FAILED) {
            this.completedAt = Instant.now();
        }
    }
}
