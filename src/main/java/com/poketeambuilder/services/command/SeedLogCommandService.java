package com.poketeambuilder.services.command;

import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.repositories.SeedLogRepository;

import com.poketeambuilder.utils.enums.SeedStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedLogCommandService {

    private static final Logger log = LoggerFactory.getLogger(SeedLogCommandService.class);

    private final SeedLogRepository seedLogRepository;
    private final SeedOrchestratorCommandService seedOrchestrator;

    public SeedLog executeSeed(String triggeredBy) {
        SeedLog seedLog = createLog(triggeredBy);

        try {
            SeedResultDto result = seedOrchestrator.seed();

            return completeLog(seedLog.getId(), result);
        } catch (Exception e) {
            log.error("Seed failed", e);

            return failLog(seedLog.getId(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected SeedLog createLog(String triggeredBy) {
        SeedLog seedLog = SeedLog.builder()
            .triggeredBy(triggeredBy)
            .build();

        return seedLogRepository.save(seedLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected SeedLog completeLog(Long logId, SeedResultDto result) {
        SeedLog seedLog = seedLogRepository.getReferenceById(logId);

        seedLog.setEntriesAdded(result.entriesAdded());
        seedLog.setErrors(result.errors());
        seedLog.setStatus(SeedStatus.COMPLETED);

        return seedLogRepository.save(seedLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected SeedLog failLog(Long logId, Exception e) {
        SeedLog seedLog = seedLogRepository.getReferenceById(logId);

        seedLog.setStatus(SeedStatus.FAILED);

        return seedLogRepository.save(seedLog);
    }
}