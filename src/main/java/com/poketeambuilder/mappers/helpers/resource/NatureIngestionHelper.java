package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.utils.enums.StatName;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

/**
 * MapStruct helper bean for nature ingestion. Translates a PokeAPI stat reference enum the nature entity stores.
 */
@Component
public class NatureIngestionHelper {

    /** Resolves the stat reference to a {@link StatName}, or {@code null} when the reference is missing. */
    @Named("extractStatName")
    public StatName extractStatName(PokeApiResource statResource) {
        if (statResource == null || statResource.name() == null) {
            return null;
        }

        return StatName.fromValue(statResource.name());
    }
}
