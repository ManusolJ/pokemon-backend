package com.poketeambuilder.utils.specification;

import com.poketeambuilder.utils.enums.SearchOperation;

import java.io.Serial;

import org.springframework.data.jpa.domain.Specification;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseSpecification<T> implements Specification<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final char LIKE_ESCAPE = '\\';

    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(@Nonnull Root<T> root, @Nullable CriteriaQuery<?> query, @Nonnull CriteriaBuilder cb) {

        Path<?> path = buildPath(root, criteria.key());

        Object value = criteria.value();

        SearchOperation operation = criteria.operation();

        return switch (operation) {
            case EQUAL -> value == null
                    ? cb.isNull(path)
                    : cb.equal(path, value);

            case LIKE -> {
                validateString(value, "LIKE");
                yield cb.like(cb.lower(path.as(String.class)), containsPattern((String) value), LIKE_ESCAPE);
            }

            case IS_NULL -> cb.isNull(path);

            case GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL -> buildComparablePredicate(operation, path, value, cb);
        };
    }

    private Path<?> buildPath(Root<T> root, String key) {
        if (!key.contains(".")) {
            return root.get(key);
        }

        String[] parts = key.split("\\.", 2);
        Join<Object, Object> join = root.join(parts[0], JoinType.LEFT);
        return join.get(parts[1]);
    }

    /**
     * Wraps a search term in wildcards, escaping any the caller supplied. Left unescaped,
     * {@code %} and {@code _} in user input act as pattern metacharacters — so a search for
     * {@code a_b} would also match {@code aab}, and a term of {@code %} would match the whole
     * table.
     */
    private String containsPattern(String value) {
        String escaped = value.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return "%" + escaped + "%";
    }

    private void validateString(Object value, String operation) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(operation + " operation requires a non-null String value");
        }
    }

    private void validateComparable(Object value, String operation) {
        if (value == null) {
            throw new IllegalArgumentException(operation + " operation requires a non-null value");
        }
        if (!(value instanceof Comparable<?>)) {
            throw new IllegalArgumentException(operation + " operation requires a Comparable value");
        }
    }

    @SuppressWarnings("unchecked")
    private <Y extends Comparable<? super Y>> Predicate buildComparablePredicate(
            SearchOperation op, Path<?> path, Object value, CriteriaBuilder cb) {

        validateComparable(value, op.name());

        Path<Y> typedPath = (Path<Y>) path;
        Y typedValue = (Y) value;

        return op == SearchOperation.GREATER_THAN_OR_EQUAL
                ? cb.greaterThanOrEqualTo(typedPath, typedValue)
                : cb.lessThanOrEqualTo(typedPath, typedValue);
    }
}
