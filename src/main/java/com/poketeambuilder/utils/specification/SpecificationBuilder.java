package com.poketeambuilder.utils.specification;

import com.poketeambuilder.utils.enums.SearchOperation;

import java.util.List;
import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuilder<T> {

    private final List<SearchCriteria> criteriaList = new ArrayList<>();

    public SpecificationBuilder<T> with(String key, Object value, Object valueTo, SearchOperation operation) {
        criteriaList.add(new SearchCriteria(key, value, valueTo, operation));
        return this;
    }

    public SpecificationBuilder<T> between(String key, Object valueFrom, Object valueTo) {
        criteriaList.add(new SearchCriteria(key, valueFrom, valueTo, SearchOperation.BETWEEN));
        return this;
    }

    public Specification<T> build() {
        if (criteriaList.isEmpty()) {
            return null;
        }

        Specification<T> spec = Specification.unrestricted();

        for (SearchCriteria criteria : criteriaList) {
            BaseSpecification<T> baseSpec = new BaseSpecification<>(criteria);
            spec = spec.and(baseSpec);
        }

        return spec;
    }
}
