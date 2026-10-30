-- =============================================================================
-- V26__team_like_count_trigger.sql
-- Moves maintenance of the denormalised `team.like_count` into the database.
--
-- The application used to bump the counter itself on like / unlike, but likes
-- also disappear through `team_like.user_id ... ON DELETE CASCADE` when an account
-- is hard-deleted or purged by the monthly cleanup, and nothing decremented the
-- counter then. A row trigger sees every insert and delete, cascades included.
-- =============================================================================

CREATE OR REPLACE FUNCTION sync_team_like_count() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE team SET like_count = like_count + 1 WHERE id = NEW.team_id;
        RETURN NEW;
    END IF;

    -- When the team itself is being deleted its row is already gone and this is a no-op.
    UPDATE team SET like_count = GREATEST(like_count - 1, 0) WHERE id = OLD.team_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_team_like_count
    AFTER INSERT OR DELETE ON team_like
    FOR EACH ROW EXECUTE FUNCTION sync_team_like_count();

-- One-off reconciliation of whatever drift has already built up.
UPDATE team t
SET like_count = counted.likes
FROM (
    SELECT t2.id, count(tl.team_id) AS likes
    FROM team t2
    LEFT JOIN team_like tl ON tl.team_id = t2.id
    GROUP BY t2.id
) counted
WHERE counted.id = t.id
  AND t.like_count <> counted.likes;
