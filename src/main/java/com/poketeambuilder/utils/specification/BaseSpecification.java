package com.poketeambuilder.utils.specification;

import com.poketeambuilder.utils.enums.SearchOperation;

import java.util.Collection;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @Nullable CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {

        String key = criteria.getKey();
        Object value = criteria.getValue();
        Object valueTo = criteria.getValueTo();
        SearchOperation operation = criteria.getOperation();

        Path<?> path = buildPath(root, key);

        try {
            return switch (operation) {
            case EQUAL -> {
                if (value == null) {
                    yield cb.isNull(path);
                }

                yield cb.equal(path, value);
            }

            case NOT_EQUAL -> {
                if (value == null) {
                    yield cb.isNotNull(path);
                }

                yield cb.notEqual(path, value);
            }

            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, BETWEEN -> buildComparablePredicate(operation, path, value, valueTo, cb);

            case LIKE -> {
                validateString(value, "LIKE");

                yield cb.like(cb.lower(path.as(String.class)), ("%" + value + "%").toLowerCase().trim());
            }

            case STARTS_WITH -> {
                validateString(value, "STARTS_WITH");

                yield cb.like(cb.lower(path.as(String.class)), (value + "%").toLowerCase().trim());
            }

            case ENDS_WITH -> {
                validateString(value, "ENDS_WITH");

                yield cb.like(cb.lower(path.as(String.class)), ("%" + value).toLowerCase().trim());
            }

            case IN -> {
                if (!(value instanceof Collection<?> collection)) {
                    throw new IllegalArgumentException("IN operation requires a Collection value");
                }

                if (collection.isEmpty()) {
                    yield cb.disjunction();
                }

                CriteriaBuilder.In<Object> in = cb.in(path);
                collection.forEach(in::value);
                yield in;
            }

            case NOT_IN -> {
                if (!(value instanceof Collection<?> collectionNot)) {
                    throw new IllegalArgumentException("NOT_IN operation requires a Collection value");
                }

                if (collectionNot.isEmpty()) {
                    yield cb.conjunction();
                }

                CriteriaBuilder.In<Object> notIn = cb.in(path);
                collectionNot.forEach(notIn::value);
                yield cb.not(notIn);
            }

            case IS_NULL -> cb.isNull(path);

            case IS_NOT_NULL -> cb.isNotNull(path);
            };
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("Invalid type for operation %s on field '%s'. Expected: %s", operation, key, getExpectedType(operation)), e);
        }
    }

    private Path<?> buildPath(Root<T> root, String key) {
        if (!key.contains(".")) {
            return root.get(key);
        }

        String[] parts = key.split("\\.");

        Path<?> path = root;

        for (String part : parts) {
            path = path.get(part);
        }

        return path;
    }

    private void validateComparable(Object value, String operation) {
        if (value == null) {
            throw new IllegalArgumentException(operation + " operation requires a non-null value");
        }

        if (!(value instanceof Comparable<?>)) {
            throw new IllegalArgumentException(operation + " operation requires a Comparable value");
        }
    }

    private void validateString(Object value, String operation) {
        if (value == null) {
            throw new IllegalArgumentException(operation + " operation requires a non-null String value");
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException(operation + " operation requires a String value");
        }
    }

    private String getExpectedType(SearchOperation op) {
        return switch (op) {
        case EQUAL, NOT_EQUAL, IN, NOT_IN -> "Any type";
        case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, BETWEEN -> "Comparable";
        case LIKE, STARTS_WITH, ENDS_WITH -> "String";
        case IS_NULL, IS_NOT_NULL -> "No value";
        };
    }

    private <Y extends Comparable<? super Y>> Predicate buildComparablePredicate(SearchOperation op, Path<?> path, Object value, Object valueTo, CriteriaBuilder cb) {

        validateComparable(value, op.name());

        if (op == SearchOperation.BETWEEN) {
            if (valueTo == null) {
                throw new IllegalArgumentException("BETWEEN operation requires two non-null values");
            }

            validateComparable(valueTo, op.name());
        }

        Path<Y> typedPath = (Path<Y>) path;

        Y typedValue = (Y) value;

        return switch (op) {
        case GREATER_THAN -> cb.greaterThan(typedPath, typedValue);
        case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo(typedPath, typedValue);
        case LESS_THAN -> cb.lessThan(typedPath, typedValue);
        case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo(typedPath, typedValue);
        case BETWEEN -> {
            Y typedValueTo = (Y) valueTo;
            yield cb.between(typedPath, typedValue, typedValueTo);
        }
        default -> throw new UnsupportedOperationException("Unsupported comparable op: " + op);
        };
    }
}