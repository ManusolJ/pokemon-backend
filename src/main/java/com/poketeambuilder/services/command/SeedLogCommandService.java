package com.poketeambuilder.services.command;

import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

import com.poketeambuilder.repositories.SeedLogRepository;

import com.poketeambuilder.utils.enums.SeedStatus;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.core.task.TaskExecutor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Wraps a seed run in a {@link SeedLog} record so admins can audit triggered ingest jobs.
 *
 * <p>The seed pipeline is fired-and-polled: {@link #triggerSeed(String)} commits a
 * {@code RUNNING} log row, dispatches the heavy work to a background virtual thread, and
 * returns immediately so the HTTP caller doesn't sit on a connection for minutes (and
 * doesn't get cut by intermediaries like Cloudflare's tunnel timeout). Admins poll
 * {@code GET /api/admin/seed-logs/filter} to watch the status flip to
 * {@code COMPLETED} / {@code FAILED}.</p>
 *
 * <p>The three log writes (start / complete / fail) each run in their own independent
 * transaction (via {@code requiresNewTransactionTemplate}) so the log row commits even
 * if the surrounding seed pipeline blows up.</p>
 */
@Slf4j
@Service
public class SeedLogCommandService {

    private final TaskExecutor taskExecutor;
    private final SeedLogRepository seedLogRepository;
    private final SeedOrchestratorCommandService seedOrchestrator;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public SeedLogCommandService(SeedLogRepository seedLogRepository, SeedOrchestratorCommandService seedOrchestrator, @Qualifier("requiresNewTransactionTemplate") TransactionTemplate requiresNewTransactionTemplate, @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.seedOrchestrator = seedOrchestrator;
        this.seedLogRepository = seedLogRepository;
        this.requiresNewTransactionTemplate = requiresNewTransactionTemplate;
    }

    /**
     * Commits a {@code RUNNING} log row, dispatches the seed pipeline on a background
     * thread, and returns the just-created log so the caller can poll it. The returned row
     * is in {@code RUNNING} status. Subsequent updates land in the same row via
     * {@link #completeLog(Long, SeedResultDto)} or {@link #failLog(Long)}.
     *
     * @throws ResourceAlreadyExistsException if another seed run is already in flight
     */
    public SeedLog triggerSeed(String triggeredBy) {
        seedLogRepository.findFirstByStatusOrderByStartedAtDesc(SeedStatus.RUNNING)
                .ifPresent(running -> {
                    throw new ResourceAlreadyExistsException(
                            "A seed run is already in progress (log id=" + running.getId()
                                    + "). Poll /api/admin/seed-logs/filter for its status.");
                });

        SeedLog seedLog = createLog(triggeredBy);
        Long logId = seedLog.getId();

        taskExecutor.execute(() -> runSeed(logId));

        return seedLog;
    }

    /** Background-thread body. Runs the orchestrator and updates the log accordingly. */
    private void runSeed(Long logId) {
        try {
            SeedResultDto result = seedOrchestrator.seed();
            completeLog(logId, result);
        } catch (Exception e) {
            log.error("Seed run {} failed", logId, e);
            failLog(logId);
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

    private void completeLog(Long logId, SeedResultDto result) {
        requiresNewTransactionTemplate.execute(status -> {
            SeedLog seedLog = seedLogRepository.getReferenceById(logId);
            seedLog.setEntriesAdded(result.entriesAdded());
            seedLog.setErrors(result.errors());
            seedLog.setStatus(SeedStatus.COMPLETED);
            return seedLogRepository.save(seedLog);
        });
    }

    private void failLog(Long logId) {
        requiresNewTransactionTemplate.execute(status -> {
            SeedLog seedLog = seedLogRepository.getReferenceById(logId);
            seedLog.setStatus(SeedStatus.FAILED);
            return seedLogRepository.save(seedLog);
        });
    }
}
