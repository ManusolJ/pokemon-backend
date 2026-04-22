package com.poketeambuilder.dtos.pokeapi.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemSprite(@JsonProperty("default") String defaultSprite) {
    
}
