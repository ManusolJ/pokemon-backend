package com.poketeambuilder.mappers.helpers.shared;

import org.mapstruct.Named;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Normalizes the PokeAPI {@code order} field for both {@code Pokemon} and {@code PokemonSpecies}.
 * Upstream returns {@code -1} for entries that don't belong to a canonical evolution-line
 * position (alternate forms, megas, gmax variants, etec) or we store {@code null} instead so the
 * column can be sorted predictably.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PokemonOrderNormalizer {

    /**
     * Returns the supplied {@code order} as-is for non-negative values; otherwise {@code null}.
     */
    @Named("normalizePokemonOrder")
    public static Integer normalizePokemonOrder(Integer order) {
        return order == null || order < 0 ? null : order;
    }
}
