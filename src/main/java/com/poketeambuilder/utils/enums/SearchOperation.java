package com.poketeambuilder.utils.enums;

import org.springframework.data.jpa.domain.Specification;

import com.poketeambuilder.utils.specification.SpecificationBuilder;

/**
 * Comparison operators used by
 * {@link SpecificationBuilder} when assembling JPA
 * {@link Specification}s from filter DTOs. Pure
 * internal enum — never persisted, never sent over the wire — so it intentionally skips the
 * {@link ValuedEnum} pattern.
 */
public enum SearchOperation {
    IN,
    LIKE,
    EQUAL,
    NOT_IN,
    BETWEEN,
    IS_NULL,
    ENDS_WITH,
    NOT_EQUAL,
    LESS_THAN,
    IS_NOT_NULL,
    STARTS_WITH,
    GREATER_THAN,
    LESS_THAN_OR_EQUAL,
    GREATER_THAN_OR_EQUAL
}
