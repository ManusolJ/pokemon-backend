package com.poketeambuilder.mappers.common;

/**
 * Marker trait for mappers that produce the compact summary DTO for an entity — the shape
 * served by listings, embeds, and any other "this is enough to identify and preview" use.
 *
 * @param <E> entity type
 * @param <S> summary DTO type
 */
public interface SummaryMapper<E, S> {

    /** Maps the entity to its summary DTO. Returns {@code null} when {@code entity} is null. */
    S toSummaryDto(E entity);
}
