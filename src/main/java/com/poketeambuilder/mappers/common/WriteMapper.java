package com.poketeambuilder.mappers.common;

/**
 * Marker trait for mappers that build an entity from a front-end create payload. Generated
 * mappers typically leave id, audit fields, and resolved entity references unset — those are
 * filled in by the calling service.
 *
 * @param <E> entity type
 * @param <C> create DTO type
 */
public interface WriteMapper<E, C> {

    /** Builds a fresh entity from the supplied create DTO. */
    E toEntity(C createDto);
}
