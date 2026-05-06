-- =============================================================================
-- V18__password_reset_token_table.sql
-- Create a new table to store password reset tokens, enabling secure password recovery functionality.
-- =============================================================================


CREATE TABLE password_reset_token (
    id          BIGSERIAL    PRIMARY KEY,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_token_hash    ON password_reset_token(token_hash);
CREATE INDEX idx_password_reset_token_user_id ON password_reset_token(user_id);