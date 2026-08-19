-- =============================================================================
-- V24__single_running_seed_log.sql
-- Enforces "at most one seed run in flight" in the database.
--
-- The application checked for a running row and then inserted, which two
-- concurrent admin requests can both pass. A partial unique index closes the
-- window; the service translates the resulting violation into its 409.
--
-- Status values are written Title-cased by SeedStatusConverter, while V1 left a
-- column default of 'RUNNING'. Both spellings are folded with upper().
-- =============================================================================

-- Any row still marked running predates this index; only the most recent can be
-- genuine, and a restart would have orphaned it anyway.
UPDATE seed_log
SET status = 'Failed',
    completed_at = COALESCE(completed_at, NOW())
WHERE upper(status) = 'RUNNING'
  AND id <> (SELECT MAX(id) FROM seed_log WHERE upper(status) = 'RUNNING');

CREATE UNIQUE INDEX uq_seed_log_single_running
    ON seed_log ((upper(status)))
    WHERE upper(status) = 'RUNNING';
