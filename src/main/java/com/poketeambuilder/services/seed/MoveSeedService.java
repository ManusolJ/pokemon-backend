package com.poketeambuilder.services.seed;

import java.util.List;
import java.util.ArrayList;

import com.poketeambuilder.entities.Move;
import com.poketeambuilder.entities.Type;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.move.MoveApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.implementation.MoveMapper;

import com.poketeambuilder.repositories.MoveRepository;
import com.poketeambuilder.repositories.TypeRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the {@code move} table from PokeAPI. Filters out non-canon move ids before fetching
 * individual resources.
 */
@Slf4j
@Service
public class MoveSeedService {

    private static final String MOVE_ENDPOINT = "/move";
    private static final int NON_CANON_MOVE_ID_THRESHOLD = 10000;

    private final MoveMapper moveMapper;
    private final TypeRepository typeRepository;
    private final MoveRepository moveRepository;

    private final PokeApiClient pokeApiClient;
    private final TransactionTemplate transactionTemplate;

    public MoveSeedService(MoveMapper moveMapper, TypeRepository typeRepository, MoveRepository moveRepository, PokeApiClient pokeApiClient, TransactionTemplate transactionTemplate) {
        this.moveMapper = moveMapper;
        this.pokeApiClient = pokeApiClient;
        this.typeRepository = typeRepository;
        this.moveRepository = moveRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public SeedResultDto seed() {
        FetchResult fetched = fetchAll();
        PersistResult persisted = transactionTemplate.execute(status -> persist(fetched.apiDtos(), fetched.errors()));
        log.info("Seeded {} moves ({} errors)", persisted.saved(), persisted.errors());
        return SeedResultDto.of(persisted.saved(), persisted.errors());
    }

    @Transactional
    public void clearSeedData() {
        moveRepository.deleteAllInBatch();
    }

    private FetchResult fetchAll() {
        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(MOVE_ENDPOINT).stream()
                .filter(resource -> {
                    Integer id = resource.extractId();
                    return id != null && id <= NON_CANON_MOVE_ID_THRESHOLD;
                })
                .toList();

        List<MoveApiDto> apiDtos = new ArrayList<>();
        int errors = 0;

        for (PokeApiResource resource : resources) {
            try {
                apiDtos.add(pokeApiClient.fetchResource(resource.url(), MoveApiDto.class));
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch move resource: {}", resource.url(), e);
            }
        }

        return new FetchResult(apiDtos, errors);
    }

    private PersistResult persist(List<MoveApiDto> apiDtos, int initialErrors) {
        List<Move> entities = new ArrayList<>();
        int errors = initialErrors;

        for (MoveApiDto dto : apiDtos) {
            try {
                Type type = typeRepository.getReferenceById(dto.type().extractId());
                Move move = moveMapper.toEntity(dto);
                move.setType(type);
                entities.add(move);
            } catch (Exception e) {
                errors++;
                log.error("Failed to map move DTO: {}", dto.name(), e);
            }
        }

        moveRepository.saveAll(entities);

        return new PersistResult(entities.size(), errors);
    }

    private record FetchResult(List<MoveApiDto> apiDtos, int errors) {}

    private record PersistResult(int saved, int errors) {}
}
