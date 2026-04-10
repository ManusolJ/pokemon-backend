package com.poketeambuilder.entities;

import com.poketeambuilder.entities.compositeIDs.TeamLikeId;

import java.time.Instant;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.MapsId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.PrePersist;

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
@Table(name = "team_like")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamLike {
    
    @EmbeddedId
    @Builder.Default
    @EqualsAndHashCode.Include
    private TeamLikeId id = new TeamLikeId();

    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser user;
    
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }
}
