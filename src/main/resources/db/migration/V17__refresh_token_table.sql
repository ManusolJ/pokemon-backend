-- =============================================================================
-- V17__refresh_token_table.sql
-- Create a new table to store refresh tokens for user authentication, allowing for secure token management and revocation.
-- =============================================================================

CREATE TABLE refresh_token (
    id          BIGSERIAL    PRIMARY KEY,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    family_id   UUID         NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_user_id   ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_family_id ON refresh_token(family_id);
CREATE INDEX idx_refresh_token_hash      ON refresh_token(token_hash);