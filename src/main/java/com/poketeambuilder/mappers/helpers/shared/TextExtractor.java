package com.poketeambuilder.mappers.helpers.shared;

import java.util.List;

import com.poketeambuilder.utils.pokeapi.LocalizedEntries;
import com.poketeambuilder.utils.pokeapi.FlavorTextFallback;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.MoveFlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.ItemFlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.AbilityFlavorTextEntry;

import org.mapstruct.Named;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Pulls localized strings out of PokeAPI's per-language / per-version arrays — effect text
 * for abilities and items, flavor text for everything else. Always selects English and runs
 * the result through {@link TextSanitizer#clean(String)} so the persisted value is single-line.
 *
 * <p>Static utility class. MapStruct binds via {@code uses = TextExtractor.class}; no Spring
 * bean is needed.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextExtractor {

    @Named("extractEffect")
    public static String extractEffect(List<EffectEntry> effectEntries) {
        return LocalizedEntries.english(effectEntries)
                .map(EffectEntry::effect)
                .map(TextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractShortEffect")
    public static String extractShortEffect(List<EffectEntry> effectEntries) {
        return LocalizedEntries.english(effectEntries)
                .map(EffectEntry::shortEffect)
                .map(TextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractAbilityFlavorText")
    public static String extractAbilityFlavorText(List<AbilityFlavorTextEntry> flavorTextEntries) {
        return FlavorTextFallback.pickBestForAbility(flavorTextEntries)
                .map(AbilityFlavorTextEntry::flavorText)
                .map(TextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractItemFlavorText")
    public static String extractItemFlavorText(List<ItemFlavorTextEntry> flavorTextEntries) {
        return FlavorTextFallback.pickBestForItem(flavorTextEntries)
                .map(ItemFlavorTextEntry::text)
                .map(TextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractMoveFlavorText")
    public static String extractMoveFlavorText(List<MoveFlavorTextEntry> flavorTextEntries) {
        return FlavorTextFallback.pickBestForMove(flavorTextEntries)
                .map(MoveFlavorTextEntry::flavorText)
                .map(TextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractSpeciesFlavorText")
    public static String extractSpeciesFlavorText(List<FlavorTextEntry> flavorTextEntries) {
        return FlavorTextFallback.pickBestForSpecies(flavorTextEntries)
                .map(FlavorTextEntry::flavorText)
                .map(TextSanitizer::clean)
                .orElse(null);
    }
}
