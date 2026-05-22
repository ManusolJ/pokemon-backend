package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.PokemonGender;

import jakarta.persistence.Converter;

/** JPA converter for {@link PokemonGender} columns. Activated globally via {@code autoApply}. */
@Converter(autoApply = true)
public class GenderConverter extends ValuedEnumConverter<PokemonGender> {

    @Override
    protected PokemonGender fromValue(String value) {
        return PokemonGender.fromValue(value);
    }
}
