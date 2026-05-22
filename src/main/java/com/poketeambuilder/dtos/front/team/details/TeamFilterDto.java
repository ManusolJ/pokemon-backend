package com.poketeambuilder.dtos.front.team.details;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;
import lombok.Setter;

/**
 * Filter payload for team listings. The {@link #copy()} factory exists so callers can layer overrides
 * (e.g. "force {@code isPublic = true}" in the public listing) without mutating the original
 * request payload.
 */
@Getter
@Setter
public class TeamFilterDto implements FilterDtoInterface {

    private Long id;

    private Long userId;

    private String slug;

    private String name;

    private String nameExact;

    private Boolean isPublic;

    /** Returns a shallow copy with every field carried over. Used to apply per-endpoint overrides safely. */
    public TeamFilterDto copy() {
        TeamFilterDto clone = new TeamFilterDto();
        clone.id = this.id;
        clone.userId = this.userId;
        clone.slug = this.slug;
        clone.name = this.name;
        clone.nameExact = this.nameExact;
        clone.isPublic = this.isPublic;
        return clone;
    }
}
