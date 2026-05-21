-- =============================================================================
-- V21__schema_cleanup.sql
-- 1. Align refresh_token / password_reset_token timestamps with TIMESTAMPTZ
-- 2. Drop redundant indexes already covered by UNIQUE constraints
-- 3. Add missing FK-backing indexes for hot join paths
-- 4. Rename team_pokemon.tera_type -> tera_type_id for naming consistency
-- =============================================================================

-- 1. Timestamp type alignment ------------------------------------------------
ALTER TABLE refresh_token
    ALTER COLUMN expires_at TYPE TIMESTAMPTZ USING expires_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE password_reset_token
    ALTER COLUMN expires_at TYPE TIMESTAMPTZ USING expires_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

-- 2. Drop indexes already implied by UNIQUE(token_hash) ----------------------
DROP INDEX IF EXISTS idx_refresh_token_hash;
DROP INDEX IF EXISTS idx_password_reset_token_hash;

-- 3. Missing FK indexes ------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_team_like_team          ON team_like(team_id);
CREATE INDEX IF NOT EXISTS idx_team_pokemon_pokemon   ON team_pokemon(pokemon_id);
CREATE INDEX IF NOT EXISTS idx_team_pokemon_move_move ON team_pokemon_move(move_id);

-- 4. Rename tera_type -> tera_type_id ---------------------------------------
ALTER TABLE team_pokemon RENAME COLUMN tera_type TO tera_type_id;
