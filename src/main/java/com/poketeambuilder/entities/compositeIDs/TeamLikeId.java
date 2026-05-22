package com.poketeambuilder.entities.compositeIDs;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Composite identifier for {@link com.poketeambuilder.entities.TeamLike}. Field order
 * mirrors the database primary key {@code (user_id, team_id)}.
 */
@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TeamLikeId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;
}
