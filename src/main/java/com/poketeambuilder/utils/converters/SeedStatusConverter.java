package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.SeedStatus;

import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;

@Converter(autoApply = true)
public class SeedStatusConverter implements AttributeConverter<SeedStatus, String> {

    @Override
    public String convertToDatabaseColumn(SeedStatus value) {
        return value != null ? value.getValue() : null;
    }

    @Override
    public SeedStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? SeedStatus.fromValue(dbData) : null;

    }
}
