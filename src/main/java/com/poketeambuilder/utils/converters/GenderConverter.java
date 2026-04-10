package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.PokemonGender;

import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<PokemonGender, String> {

    @Override
    public String convertToDatabaseColumn(PokemonGender attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public PokemonGender convertToEntityAttribute(String dbData) {
        return dbData != null ? PokemonGender.fromValue(dbData) : null;
    }
}
