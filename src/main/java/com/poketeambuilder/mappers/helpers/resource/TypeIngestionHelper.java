package com.poketeambuilder.mappers.helpers.resource;

import java.util.List;
import java.util.Collections;
import java.util.function.Function;

import com.poketeambuilder.dtos.pokeapi.type.TypeApiDto;
import com.poketeambuilder.dtos.pokeapi.type.DamageRelations;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import org.springframework.stereotype.Component;

/**
 * Helper that extracts attacking-direction damage relations from a PokeAPI type payload.
 */
@Component
public class TypeIngestionHelper {

    /** Returns the list of types this type deals 0× damage to. */
    public List<PokeApiResource> noDamageTo(TypeApiDto dto) {
        return safeList(dto.damageRelations(), DamageRelations::noDamageTo);
    }

    /** Returns the list of types this type deals 0.5× damage to. */
    public List<PokeApiResource> halfDamageTo(TypeApiDto dto) {
        return safeList(dto.damageRelations(), DamageRelations::halfDamageTo);
    }

    /** Returns the list of types this type deals 2× damage to. */
    public List<PokeApiResource> doubleDamageTo(TypeApiDto dto) {
        return safeList(dto.damageRelations(), DamageRelations::doubleDamageTo);
    }

    /** Applies {@code extractor} to {@code relations}, substituting an empty list for any null along the way. */
    private List<PokeApiResource> safeList(DamageRelations relations, Function<DamageRelations, List<PokeApiResource>> extractor) {
        if (relations == null) {
            return Collections.emptyList();
        }

        List<PokeApiResource> list = extractor.apply(relations);

        return list == null ? Collections.emptyList() : list;
    }
}
