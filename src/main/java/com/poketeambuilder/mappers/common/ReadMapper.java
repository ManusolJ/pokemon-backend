package com.poketeambuilder.mappers.common;

/**
 * Marker trait for mappers that produce the "full" read DTO for an entity — the shape served
 * by detail endpoints, with every persisted field present.
 *
 * @param <E> entity type
 * @param <R> read DTO type
 */
public interface ReadMapper<E, R> {

    /** Maps the entity to its full read DTO. Returns {@code null} when {@code entity} is null. */
    R toReadDto(E entity);
}
