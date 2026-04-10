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

import jakarta.validation.constraints.NotNull;

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
@Table(name = "seed_log")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeedLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotNull
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

    @PrePersist
    protected void onCreate() {
        this.startedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.status == SeedStatus.COMPLETED || this.status == SeedStatus.FAILED) {
            this.completedAt = Instant.now();
        }
    }
}
