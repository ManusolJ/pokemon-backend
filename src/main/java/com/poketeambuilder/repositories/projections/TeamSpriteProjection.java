package com.poketeambuilder.repositories.projections;

/**
 * Lightweight projection of a {@code team_pokemon} row for team-listing sprite columns.
 * Avoids dragging the full join (ability, nature, item, tera type) when all the
 * caller needs is the slot order and the Pokémon's default sprite URL.
 */
public interface TeamSpriteProjection {

    /** Owning team id. */
    Long getTeamId();

    /** Slot position 1–6 — used to order the sprite list per team. */
    Integer getSlot();

    /** Default sprite URL of the Pokémon in that slot. */
    String getSpriteDefault();
}
