package com.poketeambuilder.services.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.poketeambuilder.interfaces.FilterDtoInterface;
import com.poketeambuilder.interfaces.QueryInterface;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.utils.exceptions.ResourceNotFoundException;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Transactional(readOnly = true)
public abstract class AbstractQueryService<E, ID, F extends FilterDtoInterface, R> implements QueryInterface<R, ID, F> {

    protected abstract String getEntityName();

    protected abstract ReadMapper<E, R> getMapper();

    protected abstract BaseRepository<E, ID> getRepository();

    protected abstract Specification<E> buildSpecification(@NotNull F filter);

    @Override
    public R findById(@NotNull ID id) {
        return getRepository()
                .findById(id)
                .map(getMapper()::toReadDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("%s with id '%s' not found", getEntityName(), id)));
    }

    @Override
    public Page<R> filterEntities(@Valid @NotNull F filter, @NotNull Pageable pageable) {
        Specification<E> spec = buildSpecification(filter);

        Specification<E> combined = withFetches(spec);

        return getRepository()
                .findAll(combined, pageable)
                .map(getMapper()::toReadDto);
    }

    @Override
    public long countFilteredEntities(@Valid @NotNull F filter) {
        return getRepository().count(buildSpecification(filter));
    }

    protected boolean existsById(@NotNull ID id) {
        return getRepository().existsById(id);
    }

    protected void applyFetches(Root<E> root, CriteriaQuery<?> query) {
        
    }

    private Specification<E> withFetches(Specification<E> base) {
        Specification<E> fetchSpec = (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                applyFetches(root, query);
            }
            return cb.conjunction();
        };

        return base == null ? fetchSpec : base.and(fetchSpec);
    }
}