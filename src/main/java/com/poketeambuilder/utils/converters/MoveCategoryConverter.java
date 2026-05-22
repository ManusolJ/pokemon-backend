package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.MoveCategory;

import jakarta.persistence.Converter;

/** JPA converter for {@link MoveCategory} columns. Activated globally via {@code autoApply}. */
@Converter(autoApply = true)
public class MoveCategoryConverter extends ValuedEnumConverter<MoveCategory> {

    @Override
    protected MoveCategory fromValue(String value) {
        return MoveCategory.fromValue(value);
    }
}
