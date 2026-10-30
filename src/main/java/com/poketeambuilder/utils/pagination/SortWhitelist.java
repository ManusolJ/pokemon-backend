package com.poketeambuilder.utils.pagination;

import java.util.Set;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

/**
 * Restricts a client-supplied {@link Pageable}'s sort to an explicit set of properties.
 *
 * <p>Spring Data resolves nested sort paths, so an unrestricted {@code ?sort=} lets a caller
 * order results by any reachable column - {@code owner.email} or {@code owner.role} on a team
 * listing - and read those values back from the order of the rows, a few requests per guess.</p>
 */
public final class SortWhitelist {

    private SortWhitelist() {
    }

    /**
     * Drops every sort order whose property isn't in {@code allowed}. Falls back to
     * {@code fallback} when nothing survives, so paging stays deterministic.
     */
    public static Pageable restrict(Pageable pageable, Set<String> allowed, Sort fallback) {
        if (pageable.isUnpaged()) {
            return pageable;
        }

        List<Sort.Order> kept = pageable.getSort().stream()
                .filter(order -> allowed.contains(order.getProperty()))
                .toList();

        Sort sort = kept.isEmpty() ? fallback : Sort.by(kept);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
