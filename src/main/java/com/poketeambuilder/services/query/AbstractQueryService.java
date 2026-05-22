package com.poketeambuilder.services.query;

import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;

import com.poketeambuilder.interfaces.QueryInterface;
import com.poketeambuilder.interfaces.FilterDtoInterface;

import com.poketeambuilder.repositories.BaseRepository;

import com.poketeambuilder.mappers.common.ReadMapper;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

/**
 * Generic CRUD-read base for query services. Handles caching, single-id reads with optional
 * fetch joins, filter-based pagination, and the {@code count} endpoint. Concrete services
 * plug in the entity/id/filter/DTO types plus the {@link #buildSpecification(FilterDtoInterface)}
 * predicate construction; optional {@link #applyFetches(Root, CriteriaQuery)} controls which
 * associations get JOIN-FETCHed on read paths.
 *
 * <p>{@link Transactional}{@code (readOnly = true)} is applied at the class level so every
 * concrete query service runs inside a transaction that hints Hibernate to skip dirty checks.</p>
 *
 * @param <E>  entity type
 * @param <ID> identifier type
 * @param <F>  filter DTO
 * @param <R>  read DTO
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class AbstractQueryService<E, ID, F extends FilterDtoInterface, R> implements QueryInterface<R, ID, F> {

    private static final String ID_FIELD = "id";

    private final CacheManager cacheManager;

    /** Cache region used for {@link #findById(Object)} results, or {@code null} to skip caching. */
    protected abstract String getCacheName();

    /** Human-readable entity name used in {@link ResourceNotFoundException} messages. */
    protected abstract String getEntityName();

    /** Mapper used to convert entity to read DTO. */
    protected abstract ReadMapper<E, R> getMapper();

    /** Repository used for the data access calls. */
    protected abstract BaseRepository<E, ID> getRepository();

    /** Builds the JPA {@link Specification} matching the supplied filter. */
    protected abstract Specification<E> buildSpecification(@NotNull F filter);

    @Override
    public R findById(@NotNull ID id) {
        return cached(getCacheName(), id, () -> {
            Specification<E> idSpec = (root, query, cb) -> cb.equal(root.get(ID_FIELD), id);
            Specification<E> combined = withFetches(idSpec);

            return getRepository()
                    .findOne(combined)
                    .map(getMapper()::toReadDto)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("%s with id '%s' not found", getEntityName(), id)));
        });
    }

    @Override
    public Page<R> filterEntities(@Valid @NotNull F filter, @NotNull Pageable pageable) {
        Specification<E> combined = withFetches(buildSpecification(filter));

        return getRepository()
                .findAll(combined, pageable)
                .map(getMapper()::toReadDto);
    }

    /**
     * Runs the filter spec, applies the configured fetches, then converts each row with a
     * caller-supplied mapper. Useful for endpoints that need a non-{@link R} projection
     * (summary, embed) without redoing the spec assembly.
     */
    protected <D> Page<D> filterAndMap(@Valid @NotNull F filter, @NotNull Pageable pageable, @NotNull Function<E, D> mapper) {
        Specification<E> combined = withFetches(buildSpecification(filter));

        return getRepository()
                .findAll(combined, pageable)
                .map(mapper::apply);
    }

    @Override
    public long countFilteredEntities(@Valid @NotNull F filter) {
        return getRepository().count(buildSpecification(filter));
    }

    /** Existence check by id, used by command services before applying mutations. */
    protected boolean existsById(@NotNull ID id) {
        return getRepository().existsById(id);
    }

    /**
     * Override hook for subclasses that want to JOIN-FETCH associations on read queries.
     * Called on every data query (skipped on count queries). Default is no operattion.
     */
    protected void applyFetches(Root<E> root, CriteriaQuery<?> query) {

    }

    /**
     * Wraps the supplied spec so {@link #applyFetches(Root, CriteriaQuery)} runs on data
     * queries but is skipped on count queries (joining fetches into a count would inflate
     * the row count via 1:N joins and double-execute the join).
     */
    private Specification<E> withFetches(Specification<E> base) {
        Specification<E> fetchSpec = (root, query, cb) -> {
            if (query != null && query.getResultType() != Long.class) {
                applyFetches(root, query);
            }
            return cb.conjunction();
        };

        return base == null ? fetchSpec : base.and(fetchSpec);
    }

    /**
     * Looks up {@code key} in the named Spring cache, computing via {@code loader} on miss.
     * Falls back to a direct {@code loader.get()} call when the cache name is blank or the
     * cache region is missing.
     */
    protected <T> T cached(String cacheName, Object key, Supplier<T> loader) {
        if (cacheName == null || cacheName.isBlank()) {
            return loader.get();
        }

        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            return loader.get();
        }

        T value = (T) cache.get(key, loader::get);

        return value;
    }
}
