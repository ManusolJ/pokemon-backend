package com.poketeambuilder.mappers.helpers.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Move effect text arrives with a literal {@code $effect_chance} token that has to be resolved
 * against the move's own chance value, otherwise the placeholder reaches the UI verbatim.
 */
class EffectTextSubstitutionTest {

    @Test
    @DisplayName("Substitutes the chance into the placeholder")
    void substitutesTheChance() {
        String text = "Has a $effect_chance% chance to burn the target.";

        assertThat(EffectTextSubstitution.substituteEffectChance(text, 10))
                .isEqualTo("Has a 10% chance to burn the target.");
    }

    @Test
    @DisplayName("Replaces every occurrence, not just the first")
    void substitutesEveryOccurrence() {
        String text = "$effect_chance% to freeze and $effect_chance% to flinch.";

        assertThat(EffectTextSubstitution.substituteEffectChance(text, 30))
                .isEqualTo("30% to freeze and 30% to flinch.");
    }

    @Test
    @DisplayName("Leaves the text alone when the move has no secondary effect")
    void leavesTextAloneWithoutAChance() {
        String text = "Has a $effect_chance% chance to burn the target.";

        assertThat(EffectTextSubstitution.substituteEffectChance(text, null)).isEqualTo(text);
    }

    @Test
    @DisplayName("Text without the placeholder is unaffected")
    void ignoresTextWithoutThePlaceholder() {
        String text = "Deals damage with no additional effect.";

        assertThat(EffectTextSubstitution.substituteEffectChance(text, 50)).isEqualTo(text);
    }

    @Test
    @DisplayName("Passes null text through rather than throwing")
    void passesNullThrough() {
        assertThat(EffectTextSubstitution.substituteEffectChance(null, 10)).isNull();
        assertThat(EffectTextSubstitution.substituteEffectChance(null, null)).isNull();
    }
}
