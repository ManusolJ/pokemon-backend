package com.poketeambuilder.mappers.helpers.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PokeAPI returns {@code -1} as the sort order for forms that hold no canonical position —
 * megas, regional variants, gmax. Storing that verbatim would sort them ahead of Bulbasaur;
 * null sorts them out of the way instead.
 */
class PokemonOrderNormalizerTest {

    @Test
    @DisplayName("Keeps a real position untouched")
    void keepsCanonicalOrder() {
        assertThat(PokemonOrderNormalizer.normalizePokemonOrder(151)).isEqualTo(151);
    }

    @Test
    @DisplayName("Keeps zero, which is a valid position rather than a sentinel")
    void keepsZero() {
        assertThat(PokemonOrderNormalizer.normalizePokemonOrder(0)).isZero();
    }

    @ParameterizedTest(name = "order {0} becomes null")
    @ValueSource(ints = {-1, -2, -100})
    @DisplayName("Discards the negative sentinel used for non-canonical forms")
    void discardsNegatives(int order) {
        assertThat(PokemonOrderNormalizer.normalizePokemonOrder(order)).isNull();
    }

    @Test
    @DisplayName("Passes a missing order through as null")
    void passesNullThrough() {
        assertThat(PokemonOrderNormalizer.normalizePokemonOrder(null)).isNull();
    }
}
