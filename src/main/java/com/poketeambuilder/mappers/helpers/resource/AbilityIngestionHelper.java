package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextFallback;
import com.poketeambuilder.dtos.pokeapi.common.AbilityFlavorTextEntry;

import com.poketeambuilder.dtos.pokeapi.ability.AbilityApiDto;

import com.poketeambuilder.mappers.helpers.shared.FlavorTextSanitizer;

import org.springframework.stereotype.Component;

@Component
public class AbilityIngestionHelper {

    public String extractEffect(AbilityApiDto dto) {
        return LocalizedEntries.english(dto.effectEntries())
                .map(EffectEntry::effect)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }

    public String extractDescription(AbilityApiDto dto) {
        return FlavorTextFallback.pickBestForAbility(dto.flavorTextEntries())
                .map(AbilityFlavorTextEntry::flavorText)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }
}