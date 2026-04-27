package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextFallback;
import com.poketeambuilder.dtos.pokeapi.common.AbilityFlavorTextEntry;

import com.poketeambuilder.mappers.helpers.shared.FlavorTextSanitizer;

import java.util.List;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

@Component
public class AbilityIngestionHelper {

    @Named("extractAbilityEffect")
    public String extractEffect(List<EffectEntry> effectEntries) {
        return LocalizedEntries.english(effectEntries)
                .map(EffectEntry::effect)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractAbilityDescription")
    public String extractDescription(List<AbilityFlavorTextEntry> flavorTextEntries) {
        return FlavorTextFallback.pickBestForAbility(flavorTextEntries)
                .map(AbilityFlavorTextEntry::flavorText)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }
}