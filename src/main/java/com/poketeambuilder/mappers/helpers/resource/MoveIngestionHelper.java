package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.utils.enums.MoveCategory;
import com.poketeambuilder.utils.pokeapi.LocalizedEntries;

import com.poketeambuilder.dtos.pokeapi.move.MoveApiDto;
import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.helpers.shared.TextSanitizer;
import com.poketeambuilder.mappers.helpers.shared.EffectTextSubstitution;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

/**
 * MapStruct helper bean for move ingestion. Translates the PokeAPI damage class into the
 * {@link MoveCategory} enum and resolves the effect / short-effect strings, substituting the
 * {@code $effect_chance} placeholder along the way.
 */
@Component
public class MoveIngestionHelper {

    /** Resolves the damage-class slug to a {@link MoveCategory}. */
    @Named("extractMoveCategory")
    public MoveCategory extractMoveCategory(PokeApiResource damageClass) {
        if (damageClass == null || damageClass.name() == null) {
            return null;
        }

        return MoveCategory.fromValue(damageClass.name());
    }

    /** Returns the English effect text, with the effect-chance placeholder resolved and the whole thing single-lined. */
    @Named("extractMoveEffect")
    public String extractMoveEffect(MoveApiDto dto) {
        return LocalizedEntries.english(dto.effectEntries())
                .map(EffectEntry::effect)
                .map(text -> EffectTextSubstitution.substituteEffectChance(text, dto.effectChance()))
                .map(TextSanitizer::clean)
                .orElse(null);
    }

    /** Returns the English short-effect text, with placeholder resolution + single-lining applied. */
    @Named("extractMoveShortEffect")
    public String extractMoveShortEffect(MoveApiDto dto) {
        return LocalizedEntries.english(dto.effectEntries())
                .map(EffectEntry::shortEffect)
                .map(text -> EffectTextSubstitution.substituteEffectChance(text, dto.effectChance()))
                .map(TextSanitizer::clean)
                .orElse(null);
    }
}
