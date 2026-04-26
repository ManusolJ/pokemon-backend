package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.type.TypeApiDto;
import com.poketeambuilder.dtos.pokeapi.type.DamageRelations;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import java.util.List;
import java.util.Collections;
import java.util.function.Function;

import org.springframework.stereotype.Component;

@Component
public class TypeIngestionHelper {

    public List<PokeApiResource> noDamageTo(TypeApiDto dto) {
        return safeList(dto.damageRelations(), DamageRelations::noDamageTo);
    }

    public List<PokeApiResource> halfDamageTo(TypeApiDto dto) {
        return safeList(dto.damageRelations(), DamageRelations::halfDamageTo);
    }

    public List<PokeApiResource> doubleDamageTo(TypeApiDto dto) {
        return safeList(dto.damageRelations(), DamageRelations::doubleDamageTo);
    }

    private List<PokeApiResource> safeList(DamageRelations relations, Function<DamageRelations, List<PokeApiResource>> extractor) {
        if (relations == null) {
            return Collections.emptyList();
        }

        List<PokeApiResource> list = extractor.apply(relations);
        
        return list == null ? Collections.emptyList() : list;
    }
}
