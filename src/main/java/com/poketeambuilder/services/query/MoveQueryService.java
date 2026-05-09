package com.poketeambuilder.services.query;

import com.poketeambuilder.entities.Move;
import com.poketeambuilder.entities.PokemonMove;

import com.poketeambuilder.dtos.front.move.MoveEmbedDto;
import com.poketeambuilder.dtos.front.move.MoveReadDto;
import com.poketeambuilder.dtos.front.move.MoveFilterDto;
import com.poketeambuilder.dtos.front.move.MoveSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.MoveMapper;
import com.poketeambuilder.mappers.implementation.PokemonMoveMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.MoveRepository;
import com.poketeambuilder.repositories.PokemonMoveRepository;

import com.poketeambuilder.utils.enums.MoveCategory;
import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Subquery;

import jakarta.validation.constraints.NotNull;

import org.springframework.cache.CacheManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class MoveQueryService extends AbstractQueryService<Move, Integer, MoveFilterDto, MoveReadDto> {

    private final MoveMapper moveMapper;
    private final MoveRepository moveRepository;
    private final PokemonMoveMapper pokemonMoveMapper;
    private final PokemonMoveRepository pokemonMoveRepository;

    public MoveQueryService(CacheManager cacheManager, MoveMapper moveMapper, MoveRepository moveRepository,
                            PokemonMoveMapper pokemonMoveMapper, PokemonMoveRepository pokemonMoveRepository) {
        super(cacheManager);
        this.moveMapper = moveMapper;
        this.moveRepository = moveRepository;
        this.pokemonMoveMapper = pokemonMoveMapper;
        this.pokemonMoveRepository = pokemonMoveRepository;
    }

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_TYPE_ID = "type.id";
    private static final String FIELD_POWER = "power";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_PRIORITY = "priority";
    private static final String FIELD_ACCURACY = "accuracy";

    @Override
    protected String getEntityName() {
        return "Move";
    }

    @Override
    protected String getCacheName() {
        return "moves";
    }

    @Override
    protected ReadMapper<Move, MoveReadDto> getMapper() {
        return moveMapper;
    }

    @Override
    protected BaseRepository<Move, Integer> getRepository() {
        return moveRepository;
    }

    @Override
    protected void applyFetches(Root<Move> root, CriteriaQuery<?> query) {
        root.fetch("type");
        query.distinct(true);
    }

    public Page<MoveSummaryDto> filterSummaries(MoveFilterDto filter, Pageable pageable) {
        return filterAndMap(filter, pageable, moveMapper::toSummaryDto);
    }

    public Page<MoveEmbedDto> filterEmbeds(@NotNull Integer pokemonId, @NotNull Pageable pageable) {
        return pokemonMoveRepository.findByIdPokemonId(pokemonId, pageable)
                .map(pokemonMoveMapper::toEmbedDto);
    }

    @Override
    protected Specification<Move> buildSpecification(@NotNull MoveFilterDto filter) {
        SpecificationBuilder<Move> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getId() != null) {
            builder.with(FIELD_ID, filter.getId(), SearchOperation.EQUAL);
        }
        if (filter.getName() != null && !filter.getName().isBlank()) {
            builder.with(FIELD_NAME, filter.getName(), SearchOperation.LIKE);
        }
        if (filter.getNameExact() != null && !filter.getNameExact().isBlank()) {
            builder.with(FIELD_NAME, filter.getNameExact(), SearchOperation.EQUAL);
        }
        if (filter.getTypeId() != null) {
            builder.with(FIELD_TYPE_ID, filter.getTypeId(), SearchOperation.EQUAL);
        }
        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            builder.with(FIELD_CATEGORY, MoveCategory.fromValue(filter.getCategory()), SearchOperation.EQUAL);
        }
        if (filter.getPriority() != null) {
            builder.with(FIELD_PRIORITY, filter.getPriority(), SearchOperation.EQUAL);
        }
        if (filter.getMinPower() != null) {
            builder.with(FIELD_POWER, filter.getMinPower(), SearchOperation.GREATER_THAN_OR_EQUAL);
        }
        if (filter.getMaxPower() != null) {
            builder.with(FIELD_POWER, filter.getMaxPower(), SearchOperation.LESS_THAN_OR_EQUAL);
        }
        if (filter.getMinAccuracy() != null) {
            builder.with(FIELD_ACCURACY, filter.getMinAccuracy(), SearchOperation.GREATER_THAN_OR_EQUAL);
        }
        if (filter.getMaxAccuracy() != null) {
            builder.with(FIELD_ACCURACY, filter.getMaxAccuracy(), SearchOperation.LESS_THAN_OR_EQUAL);
        }

        Specification<Move> spec = builder.build();

        if (filter.getPokemonId() != null) {
            Integer pokemonId = filter.getPokemonId();
            Specification<Move> pokemonSpec = (root, query, cb) -> {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<PokemonMove> pmRoot = subquery.from(PokemonMove.class);
                subquery.select(pmRoot.get("id").get("moveId"))
                        .where(cb.equal(pmRoot.get("id").get("pokemonId"), pokemonId));
                return root.get("id").in(subquery);
            };
            spec = spec.and(pokemonSpec);
        }

        return spec;
    }
}