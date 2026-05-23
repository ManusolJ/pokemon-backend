package com.poketeambuilder.services.command;

import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.repositories.SeedLogRepository;

import com.poketeambuilder.utils.enums.SeedStatus;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Service;

import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Wraps a seed run in a {@link SeedLog} record so admins can audit triggered ingest jobs.
 * The three log writes (start / complete / fail) each run in their own independent
 * transaction (via the {@code requiresNewTransactionTemplate}) so the log row commits even
 * if the surrounding seed pipeline blows up.
 */
@Slf4j
@Service
public class SeedLogCommandService {

    private final SeedLogRepository seedLogRepository;
    private final SeedOrchestratorCommandService seedOrchestrator;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public SeedLogCommandService(SeedLogRepository seedLogRepository,
                                 SeedOrchestratorCommandService seedOrchestrator,
                                 @Qualifier("requiresNewTransactionTemplate") TransactionTemplate requiresNewTransactionTemplate) {
        this.seedLogRepository = seedLogRepository;
        this.seedOrchestrator = seedOrchestrator;
        this.requiresNewTransactionTemplate = requiresNewTransactionTemplate;
    }

    /**
     * Runs the seed pipeline, recording start / complete / fail rows around it. The seed
     * runs outside any transaction (see {@link SeedOrchestratorCommandService}); only the
     * log writes around it are transactional.
     */
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

    private SeedLog createLog(String triggeredBy) {
        return requiresNewTransactionTemplate.execute(status -> {
            SeedLog seedLog = SeedLog.builder()
                    .triggeredBy(triggeredBy)
                    .build();
            return seedLogRepository.save(seedLog);
        });
    }

    private SeedLog completeLog(Long logId, SeedResultDto result) {
        return requiresNewTransactionTemplate.execute(status -> {
            SeedLog seedLog = seedLogRepository.getReferenceById(logId);
            seedLog.setEntriesAdded(result.entriesAdded());
            seedLog.setErrors(result.errors());
            seedLog.setStatus(SeedStatus.COMPLETED);
            return seedLogRepository.save(seedLog);
        });
    }

    private SeedLog failLog(Long logId, Exception e) {
        return requiresNewTransactionTemplate.execute(status -> {
            SeedLog seedLog = seedLogRepository.getReferenceById(logId);
            seedLog.setStatus(SeedStatus.FAILED);
            return seedLogRepository.save(seedLog);
        });
    }
}
