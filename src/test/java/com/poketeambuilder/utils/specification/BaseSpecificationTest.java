package com.poketeambuilder.utils.specification;

import com.poketeambuilder.utils.enums.SearchOperation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaBuilder;

/**
 * Covers the translation from {@link SearchCriteria} to a Criteria API predicate.
 */
class BaseSpecificationTest {

    private final Root<Object> root = mockRoot();
    private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

    @Test
    @DisplayName("LIKE escapes wildcards the caller supplied")
    void likeEscapesUserSuppliedWildcards() {
        Path<String> path = stringPath();
        Expression<String> lowered = lowered(path);

        toPredicate(new SearchCriteria("name", "a_b%c", SearchOperation.LIKE));

        verify(cb).like(eq(lowered), eq("%a\\_b\\%c%"), eq('\\'));
    }

    @Test
    @DisplayName("LIKE trims and lowercases before wrapping in wildcards")
    void likeNormalisesTheTerm() {
        Path<String> path = stringPath();
        Expression<String> lowered = lowered(path);

        toPredicate(new SearchCriteria("name", "  Pikachu  ", SearchOperation.LIKE));

        verify(cb).like(eq(lowered), eq("%pikachu%"), eq('\\'));
    }

    @Test
    @DisplayName("EQUAL with a null value becomes IS NULL rather than = null")
    void equalWithNullBecomesIsNull() {
        Path<Object> path = objectPath();

        toPredicate(new SearchCriteria("nature", null, SearchOperation.EQUAL));

        verify(cb).isNull(path);
    }

    @Test
    @DisplayName("A dotted key joins the association instead of reading a nested field")
    void dottedKeyProducesLeftJoin() {
        Join<Object, Object> join = mock(Join.class);
        Path<Object> joined = objectPathOn(join, "id");
        when(root.join("owner", JoinType.LEFT)).thenReturn(join);

        toPredicate(new SearchCriteria("owner.id", 7L, SearchOperation.EQUAL));

        verify(root).join("owner", JoinType.LEFT);
        verify(cb).equal(joined, 7L);
    }

    @Test
    @DisplayName("LIKE against a non-string value is rejected")
    void likeRejectsNonStringValues() {
        stringPath();

        assertThatThrownBy(() -> toPredicate(new SearchCriteria("name", 42, SearchOperation.LIKE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LIKE");
    }

    @Test
    @DisplayName("A range bound with a null value is rejected rather than silently dropped")
    void comparableRejectsNullValues() {
        objectPath();

        assertThatThrownBy(() -> toPredicate(new SearchCriteria("baseSpeed", null, SearchOperation.GREATER_THAN_OR_EQUAL)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GREATER_THAN_OR_EQUAL");
    }

    @Test
    @DisplayName("Range bounds map to inclusive comparisons")
    void rangeBoundsAreInclusive() {
        Path<Object> path = objectPath();

        toPredicate(new SearchCriteria("baseSpeed", 80, SearchOperation.GREATER_THAN_OR_EQUAL));
        verify(cb).greaterThanOrEqualTo(any(), eq(80));

        toPredicate(new SearchCriteria("baseSpeed", 120, SearchOperation.LESS_THAN_OR_EQUAL));
        verify(cb).lessThanOrEqualTo(any(), eq(120));

        assertThat(path).isNotNull();
    }

    private Predicate toPredicate(SearchCriteria criteria) {
        return new BaseSpecification<>(criteria).toPredicate(root, null, cb);
    }

    @SuppressWarnings("unchecked")
    private Root<Object> mockRoot() {
        return mock(Root.class);
    }

    @SuppressWarnings("unchecked")
    private Path<Object> objectPath() {
        Path<Object> path = mock(Path.class);
        when(root.get(any(String.class))).thenReturn(path);
        return path;
    }

    @SuppressWarnings("unchecked")
    private Path<Object> objectPathOn(Join<Object, Object> join, String attribute) {
        Path<Object> path = mock(Path.class);
        when(join.get(attribute)).thenReturn(path);
        return path;
    }

    @SuppressWarnings("unchecked")
    private Path<String> stringPath() {
        Path<String> path = mock(Path.class);
        Path<Object> raw = mock(Path.class);
        when(root.get(any(String.class))).thenReturn(raw);
        when(raw.as(String.class)).thenReturn(path);
        return path;
    }

    @SuppressWarnings("unchecked")
    private Expression<String> lowered(Path<String> path) {
        Expression<String> lowered = mock(Expression.class);
        when(cb.lower(path)).thenReturn(lowered);
        return lowered;
    }
}
