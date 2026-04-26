package com.poketeambuilder.mappers.helpers.resource;


import com.poketeambuilder.utils.enums.StatName;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import org.springframework.stereotype.Component;

@Component
public class NatureIngestionHelper {

    public StatName extractStatName(PokeApiResource statResource) {
        if (statResource == null || statResource.name() == null) {
            return null;
        }
        
        return StatName.fromValue(statResource.name());
    }
}
