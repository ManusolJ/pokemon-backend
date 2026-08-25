package com.poketeambuilder.mappers.helpers.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PokeAPI wraps its flavour text to a fixed column width with hard line breaks and form feeds,
 * so the raw strings arrive full of control characters. Everything persisted goes through this
 * cleaner first.
 */
class TextSanitizerTest {

    @Test
    @DisplayName("Collapses the newlines and form feeds PokeAPI wraps flavour text with")
    void collapsesControlCharacters() {
        String raw = "It has a\nvicious temper\fand attacks\nwithout warning.";

        assertThat(TextSanitizer.clean(raw))
                .isEqualTo("It has a vicious temper and attacks without warning.");
    }

    @Test
    @DisplayName("Collapses any run of whitespace to a single space")
    void collapsesWhitespaceRuns() {
        assertThat(TextSanitizer.clean("too   many \t spaces")).isEqualTo("too many spaces");
    }

    @Test
    @DisplayName("Trims the edges after collapsing")
    void trimsAfterCollapsing() {
        assertThat(TextSanitizer.clean("\n  padded  \f")).isEqualTo("padded");
    }

    @Test
    @DisplayName("Passes null through rather than throwing")
    void passesNullThrough() {
        assertThat(TextSanitizer.clean(null)).isNull();
    }

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource({
            "'',''",
            "'   ',''",
            "'single','single'",
            "'a  b','a b'",
    })
    @DisplayName("Handles empty and whitespace-only input")
    void handlesEdgeCases(String input, String expected) {
        assertThat(TextSanitizer.clean(input)).isEqualTo(expected);
    }
}
