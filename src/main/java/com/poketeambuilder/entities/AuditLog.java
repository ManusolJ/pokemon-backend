package com.poketeambuilder.entities;

import java.time.Instant;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

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
@Table(name = "audit_log")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Column(name = "username", nullable = false, length = 30)
    private String username;

    @NotBlank
    @Size(max = 255)
    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Size(max = 50)
    @Column(name = "entity", length = 50)
    private String entity;

    @Size(max = 50)
    @Column(name = "entity_id", length = 50)
    private String entityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
