package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.StatName;

import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;

@Converter(autoApply = true)
public class StatNameConverter implements AttributeConverter<StatName, String> {

    @Override
    public String convertToDatabaseColumn(StatName statName) {
        return statName == null ? null : statName.getValue();
    }

    @Override
    public StatName convertToEntityAttribute(String dbData) {
        return dbData == null ? null : StatName.fromValue(dbData);
    }
    
}
