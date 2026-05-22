package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.repository.NoRepositoryBean;

/**
 * Project-wide base for Spring Data repositories. Combines {@link JpaRepository} CRUD with
 * {@link JpaSpecificationExecutor} so any concrete repository can be passed a {@code
 * Specification<T>} for dynamic filtering without redeclaring the boilerplate.
 *
 * <p>Marked {@link NoRepositoryBean} so Spring doesn't try to instantiate it directly.</p>
 *
 * @param <T>  entity type
 * @param <ID> id type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    
}
