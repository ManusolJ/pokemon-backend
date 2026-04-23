package com.poketeambuilder.dtos.pokeapi.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
    public record PokemonSprites(
            @JsonProperty("front_default") String frontDefault,
            @JsonProperty("front_shiny")   String frontShiny,
            Other other
    ) {
        
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Other(@JsonProperty("official-artwork") OfficialArtwork officialArtwork) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OfficialArtwork(
            @JsonProperty("front_default") String frontDefault,
            @JsonProperty("front_shiny")   String frontShiny
    ) {}
}
