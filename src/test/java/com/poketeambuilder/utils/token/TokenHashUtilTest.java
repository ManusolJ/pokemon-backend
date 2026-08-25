package com.poketeambuilder.utils.token;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Refresh and password-reset tokens are stored as digests, never as the value handed to the
 * user, so a database dump doesn't yield usable sessions. Lookups hash the incoming value and
 * match on the digest, which only works if the hash is stable.
 */
class TokenHashUtilTest {

    private static final int SHA256_HEX_LENGTH = 64;

    @Test
    @DisplayName("Hashing the same token twice gives the same digest")
    void isDeterministic() {
        String token = "0f8fad5b-d9cb-469f-a165-70867728950e";

        assertThat(TokenHashUtil.sha256(token)).isEqualTo(TokenHashUtil.sha256(token));
    }

    @Test
    @DisplayName("Different tokens give different digests")
    void separatesDistinctTokens() {
        assertThat(TokenHashUtil.sha256("token-a")).isNotEqualTo(TokenHashUtil.sha256("token-b"));
    }

    @Test
    @DisplayName("Produces lower-case hex of the full digest length")
    void producesHexOfExpectedLength() {
        assertThat(TokenHashUtil.sha256("any-token"))
                .hasSize(SHA256_HEX_LENGTH)
                .matches("[0-9a-f]+");
    }

    @Test
    @DisplayName("Matches the published SHA-256 vector for the empty string")
    void matchesKnownVector() {
        assertThat(TokenHashUtil.sha256(""))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    @DisplayName("Hashes the UTF-8 bytes, so non-ASCII tokens are stable")
    void handlesNonAsciiInput() {
        assertThat(TokenHashUtil.sha256("Pokemon-Ω"))
                .isEqualTo(TokenHashUtil.sha256("Pokemon-Ω"))
                .hasSize(SHA256_HEX_LENGTH);
    }

    @Test
    @DisplayName("A null token is a programming error, not a silent empty hash")
    void rejectsNull() {
        assertThatThrownBy(() -> TokenHashUtil.sha256(null))
                .isInstanceOf(NullPointerException.class);
    }
}
