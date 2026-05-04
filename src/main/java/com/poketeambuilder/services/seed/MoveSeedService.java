package com.poketeambuilder.services.seed;

import com.poketeambuilder.entities.Move;
import com.poketeambuilder.entities.Type;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.move.MoveApiDto;

import com.poketeambuilder.mappers.implementation.MoveMapper;

import com.poketeambuilder.repositories.MoveRepository;
import com.poketeambuilder.repositories.TypeRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoveSeedService {
    
    private final static Logger log = LoggerFactory.getLogger(MoveSeedService.class);

    private final MoveMapper moveMapper;
    private final TypeRepository typeRepository;
    private final MoveRepository moveRepository;

    private final PokeApiClient pokeApiClient;

    private static final int MAX_CANON_MOVE_ID = 10000;
    private final static String MOVE_ENDPOINT = "/move";

    @Transactional
    public SeedResultDto seed() {
        int errors = 0;

        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(MOVE_ENDPOINT);

        List<MoveApiDto> apiDtos = new ArrayList<>();

        List<Move> entities = new ArrayList<>();

        for (PokeApiResource resource : resources) {
            try {
                MoveApiDto apiDto = pokeApiClient.fetchResource(resource.url(), MoveApiDto.class);

                if (apiDto.id() <= MAX_CANON_MOVE_ID) {
                    apiDtos.add(apiDto);
                }
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch move resource: {}", resource.url(), e);
            }
        }

        for (MoveApiDto dto : apiDtos) {
            try {
                Type type = typeRepository.getReferenceById(dto.type().extractId());
                Move move = moveMapper.toEntity(dto);
                move.setType(type);
                entities.add(move);
            } catch (Exception e) {
                errors++;
                log.error("Failed to map move DTO: {}", dto, e);
            }
        }

        moveRepository.saveAll(entities);

        log.info("Seeded {} moves ({} fetch errors)", entities.size(), errors);

        return new SeedResultDto(entities.size(), errors);
    }

    @Transactional
    public void clearSeedData() {
        moveRepository.deleteAllInBatch();
    }
}
