package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.MoveCategory;

import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;

@Converter(autoApply = true)
public class MoveCategoryConverter implements AttributeConverter<MoveCategory, String> {

    @Override
    public String convertToDatabaseColumn(MoveCategory moveCategory) {
        return moveCategory == null ? null : moveCategory.getValue();
    }

    @Override
    public MoveCategory convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MoveCategory.fromValue(dbData);
    }
    
}
