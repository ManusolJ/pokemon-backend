package com.poketeambuilder.utils.enums;

import com.poketeambuilder.utils.converters.ValuedEnumConverter;

/**
 * Marker for enums that have a stable, persisted "wire value" distinct from (or equal to)
 * their {@link Enum#name()}. The value is what we write to the database, send over the wire,
 * and receive from external APIs — keeping it explicit lets us rename enum constants in Java
 * without breaking storage or API contracts.
 *
 * <p>Pairs with
 * {@link ValuedEnumConverter} so a single base converter
 * can serialize any implementation without per-enum boilerplate.</p>
 */
public interface ValuedEnum {

    /** The persisted / wire representation of this enum constant. */
    String getValue();
}
