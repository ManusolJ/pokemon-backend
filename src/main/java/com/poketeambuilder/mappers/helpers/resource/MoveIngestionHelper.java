package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.utils.enums.MoveCategory;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;

import com.poketeambuilder.dtos.pokeapi.move.MoveApiDto;

import com.poketeambuilder.mappers.helpers.shared.EffectTextSubstitution;
import com.poketeambuilder.mappers.helpers.shared.TextSanitizer;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

@Component
public class MoveIngestionHelper {

    @Named("extractMoveCategory")
    public MoveCategory extractMoveCategory(PokeApiResource damageClass) {
        if (damageClass == null || damageClass.name() == null) {
            return null;
        }
        
        return MoveCategory.fromValue(damageClass.name());
    }

    @Named("extractMoveEffect")
    public String extractMoveEffect(MoveApiDto dto) {
        return LocalizedEntries.english(dto.effectEntries())
                .map(EffectEntry::effect)
                .map(text -> EffectTextSubstitution.substituteEffectChance(text, dto.effectChance()))
                .map(TextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractMoveShortEffect")
    public String extractMoveShortEffect(MoveApiDto dto) {
        return LocalizedEntries.english(dto.effectEntries())
                .map(EffectEntry::shortEffect)
                .map(text -> EffectTextSubstitution.substituteEffectChance(text, dto.effectChance()))
                .map(TextSanitizer::clean)
                .orElse(null);
    }
}