package com.poketeambuilder.dtos.REST.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiResource(String name, String url) {
    
    public Integer extractId() {
        if (url == null || url.isBlank()) return null;
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