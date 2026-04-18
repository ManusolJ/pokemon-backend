package com.poketeambuilder.utils.specification;

import com.poketeambuilder.utils.enums.SearchOperation;

import java.io.Serial;
import java.util.Collection;

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

    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(@Nonnull Root<T> root, @Nullable CriteriaQuery<?> query, @Nonnull CriteriaBuilder cb) {

        Path<?> path = buildPath(root, criteria.key());

        Object value = criteria.value();

        Object valueTo = criteria.valueTo();

        SearchOperation operation = criteria.operation();

        return switch (operation) {
            case EQUAL -> value == null
                    ? cb.isNull(path)
                    : cb.equal(path, value);

            case NOT_EQUAL -> value == null
                    ? cb.isNotNull(path)
                    : cb.notEqual(path, value);

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
                if (!(value instanceof Collection<?> collection)) {
                    throw new IllegalArgumentException("NOT_IN operation requires a Collection value");
                }

                if (collection.isEmpty()) {
                    yield cb.conjunction();
                }

                CriteriaBuilder.In<Object> notIn = cb.in(path);
                collection.forEach(notIn::value);
                yield cb.not(notIn);
            }

            case IS_NULL     -> cb.isNull(path);
            case IS_NOT_NULL -> cb.isNotNull(path);

            case GREATER_THAN, GREATER_THAN_OR_EQUAL,
                 LESS_THAN, LESS_THAN_OR_EQUAL,
                 BETWEEN -> buildComparablePredicate(operation, path, value, valueTo, cb);
        };
    }

    private Path<?> buildPath(Root<T> root, String key) {
        if (!key.contains(".")) {
            return root.get(key);
        }

        String[] parts = key.split("\\.", 2);
        Join<?, ?> join = root.join(parts[0], JoinType.LEFT);
        return join.get(parts[1]);
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
            SearchOperation op, Path<?> path, Object value, Object valueTo, CriteriaBuilder cb) {

        validateComparable(value, op.name());

        Path<Y> typedPath = (Path<Y>) path;
        Y typedValue = (Y) value;

        return switch (op) {
            case GREATER_THAN          -> cb.greaterThan(typedPath, typedValue);
            case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo(typedPath, typedValue);
            case LESS_THAN             -> cb.lessThan(typedPath, typedValue);
            case LESS_THAN_OR_EQUAL    -> cb.lessThanOrEqualTo(typedPath, typedValue);
            case BETWEEN -> {
                validateComparable(valueTo, "BETWEEN (valueTo)");
                Y typedValueTo = (Y) valueTo;
                yield cb.between(typedPath, typedValue, typedValueTo);
            }
            default -> throw new UnsupportedOperationException("Unsupported comparable operation: " + op);
        };
    }
}