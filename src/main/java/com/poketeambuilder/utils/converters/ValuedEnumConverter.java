package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.ValuedEnum;

import jakarta.persistence.AttributeConverter;

/**
 * Base for JPA converters that map a {@link ValuedEnum} to its {@link ValuedEnum#getValue()}
 * representation in the database. Subclasses only need to delegate {@link #fromValue(String)}
 * to the enum's static parser; everything else (null handling, both directions) lives here.
 *
 * <p>Concrete converters must still carry their own {@code @Converter(autoApply = true)}
 * annotation — Hibernate scans for it directly on the concrete class.</p>
 *
 * @param <E> a value-backed enum
 */
public abstract class ValuedEnumConverter<E extends Enum<E> & ValuedEnum> implements AttributeConverter<E, String> {

    /** Parses a database string back to the matching enum constant. */
    protected abstract E fromValue(String value);

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return dbData == null ? null : fromValue(dbData);
    }
}
