package com.poketeambuilder.mappers.common;

/**
 * Marker trait for mappers that build an entity from an upstream PokeAPI payload. Resolved
 * entity references (foreign keys to other Pokémon, types, abilities, etc) are intentionally
 * left for the seed services to fill in after a second lookup pass.
 *
 * @param <E> entity type
 * @param <D> PokeAPI DTO type
 */
public interface ApiMapper<E, D> {

    /** Builds an entity from the upstream PokeAPI DTO. */
    E toEntity(D dto);
}
