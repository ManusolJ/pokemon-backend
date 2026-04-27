package com.poketeambuilder.mappers.helpers.shared;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;
import com.poketeambuilder.dtos.pokeapi.common.MoveFlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextFallback;
import com.poketeambuilder.dtos.pokeapi.common.ItemFlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.AbilityFlavorTextEntry;

import java.util.List;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

@Component
public class TextExtractor {
    
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
