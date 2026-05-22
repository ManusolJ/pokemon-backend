package com.poketeambuilder.dtos.pokeapi.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The {@code {name, url}} pair PokeAPI uses to refer to any related resource. The URL
 * always ends with the resource id; {@link #extractId()} parses it so callers can resolve
 * the relationship without a second HTTP round-trip.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiResource(String name, String url) {

    /**
     * Parses the id out of {@link #url}. PokeAPI URLs end with {@code .../<resource>/<id>/}
     * (trailing slash optional). Returns {@code null} when the URL is missing or malformed —
     * callers should treat that as "unresolved reference".
     */
    public Integer extractId() {
        if (url == null || url.isBlank()) {
            return null;
        }

        try {
            String[] parts = url.split("/");
            String idPart = parts[parts.length - 1].isEmpty()
                    ? parts[parts.length - 2]
                    : parts[parts.length - 1];
            return Integer.parseInt(idPart);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
