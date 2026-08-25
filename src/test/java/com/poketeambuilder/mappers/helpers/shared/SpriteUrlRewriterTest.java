package com.poketeambuilder.mappers.helpers.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sprites are stored as paths rather than absolute URLs so the asset host can change without
 * a data migration. The front-end re-attaches its own base.
 */
class SpriteUrlRewriterTest {

    private static final String PREFIX = "https://raw.githubusercontent.com/PokeAPI/sprites/master";

    @Test
    @DisplayName("Strips the PokeAPI host, leaving a host-relative path")
    void stripsTheUpstreamPrefix() {
        assertThat(SpriteUrlRewriter.rewrite(PREFIX + "/sprites/items/leftovers.png"))
                .isEqualTo("/sprites/items/leftovers.png");
    }

    @Test
    @DisplayName("Leaves a URL from another host untouched")
    void leavesForeignUrlsAlone() {
        String other = "https://example.com/sprites/pokemon/1.png";

        assertThat(SpriteUrlRewriter.rewrite(other)).isEqualTo(other);
    }

    @Test
    @DisplayName("Treats null and blank as no sprite")
    void treatsBlankAsAbsent() {
        assertThat(SpriteUrlRewriter.rewrite(null)).isNull();
        assertThat(SpriteUrlRewriter.rewrite("")).isNull();
        assertThat(SpriteUrlRewriter.rewrite("   ")).isNull();
    }

    @Test
    @DisplayName("Rewriting an already-rewritten path is a no-op")
    void isIdempotent() {
        String once = SpriteUrlRewriter.rewrite(PREFIX + "/sprites/pokemon/25.png");

        assertThat(SpriteUrlRewriter.rewrite(once)).isEqualTo(once);
    }
}
