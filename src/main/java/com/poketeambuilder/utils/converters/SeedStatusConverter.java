package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.SeedStatus;

import jakarta.persistence.Converter;

/**
 * JPA converter for {@link SeedStatus} columns. Activated globally via {@code autoApply}.
 *
 * <p>Reads use the lenient parse: an unrecognised stored value becomes {@link SeedStatus#UNKNOWN}
 * rather than throwing, so one odd row cannot take down the whole seed-log listing.</p>
 */
@Converter(autoApply = true)
public class SeedStatusConverter extends ValuedEnumConverter<SeedStatus> {

    @Override
    protected SeedStatus fromValue(String value) {
        return SeedStatus.fromDatabaseValue(value);
    }
}
