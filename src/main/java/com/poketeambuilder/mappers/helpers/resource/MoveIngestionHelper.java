package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.utils.enums.MoveCategory;

import com.poketeambuilder.dtos.pokeapi.move.MoveApiDto;
import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextFallback;
import com.poketeambuilder.dtos.pokeapi.common.MoveFlavorTextEntry;

import com.poketeambuilder.mappers.helpers.shared.FlavorTextSanitizer;
import com.poketeambuilder.mappers.helpers.shared.EffectTextSubstitution;

import java.util.List;

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

    @Named("extractEffectDescription")
    public String extractEffectDescription(MoveApiDto dto) {
        return LocalizedEntries.english(dto.effectEntries())
                .map(EffectEntry::shortEffect)
                .map(text -> EffectTextSubstitution.substituteEffectChance(text, dto.effectChance()))
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractMoveFlavorText")
    public String extractFlavorText(List<MoveFlavorTextEntry> flavorTextEntries) {
        return FlavorTextFallback.pickBestForMove(flavorTextEntries)
                .map(MoveFlavorTextEntry::flavorText)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }
}