package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.StatName;

import jakarta.persistence.Converter;

/** JPA converter for {@link StatName} columns. Activated globally via {@code autoApply}. */
@Converter(autoApply = true)
public class StatNameConverter extends ValuedEnumConverter<StatName> {

    @Override
    protected StatName fromValue(String value) {
        return StatName.fromValue(value);
    }
}
