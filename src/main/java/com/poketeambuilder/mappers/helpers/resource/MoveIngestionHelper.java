package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.utils.enums.MoveCategory;

import com.poketeambuilder.dtos.pokeapi.move.MoveApiDto;
import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;

import com.poketeambuilder.mappers.helpers.shared.FlavorTextSanitizer;
import com.poketeambuilder.mappers.helpers.shared.EffectTextSubstitution;

import org.springframework.stereotype.Component;

@Component
public class MoveIngestionHelper {

    public MoveCategory extractCategory(MoveApiDto dto) {
        if (dto.damageClass() == null || dto.damageClass().name() == null) {
            return null;
        }
        
        return MoveCategory.fromValue(dto.damageClass().name());
    }

    public String extractEffectDescription(MoveApiDto dto) {
        return LocalizedEntries.english(dto.effectEntries())
                .map(EffectEntry::shortEffect)
                .map(text -> EffectTextSubstitution.substituteEffectChance(text, dto.effectChance()))
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }
}
