package com.poketeambuilder.mappers.common;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Project-wide MapStruct configuration applied to every {@code @Mapper(config = …)}. Choices:
 *
 * <ul>
 *   <li>{@code componentModel = "spring"} — generated mappers are Spring beans, so they can
 *       be {@code @Autowired} and use other Spring components ({@code *IngestionHelper}).</li>
 *   <li>{@code unmappedTargetPolicy = IGNORE} — unmapped target properties don't fail the
 *       build; the {@code toEntity} mappers deliberately leave entity references unset so
 *       the service layer can resolve them.</li>
 *   <li>{@code nullValueCheckStrategy = ALWAYS} — null checks before invoking source
 *       methods, even on primitive accessors.</li>
 *   <li>{@code nullValuePropertyMappingStrategy = IGNORE} — when updating an entity with a
 *       null property, leave the target value alone. Critical for {@code PATCH} flows.</li>
 * </ul>
 */
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapperConfiguration {
}
