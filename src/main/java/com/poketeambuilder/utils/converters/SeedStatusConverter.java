package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.SeedStatus;

import jakarta.persistence.Converter;

/** JPA converter for {@link SeedStatus} columns. Activated globally via {@code autoApply}. */
@Converter(autoApply = true)
public class SeedStatusConverter extends ValuedEnumConverter<SeedStatus> {

    @Override
    protected SeedStatus fromValue(String value) {
        return SeedStatus.fromValue(value);
    }
}
