package com.poketeambuilder.dtos.pokeapi.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemSprite(@JsonProperty("default") String defaultSprite) {
    
}
