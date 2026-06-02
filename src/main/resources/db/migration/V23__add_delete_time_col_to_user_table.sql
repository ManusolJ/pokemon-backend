-- =============================================================================
-- V23__add_delete_time_col_to_user_table.sql
-- Adds the `deleted_at` tombstone column to `app_user` and converts the
-- username/email uniqueness constraints into partial indexes scoped to
-- non-tombstoned rows. After this migration:
--   - `deleted_at IS NULL`  → active or admin-disabled user (counts for uniqueness)
--   - `deleted_at IS NOT NULL` → tombstoned user (username/email freed for reuse)
-- =============================================================================

ALTER TABLE app_user
    ADD COLUMN deleted_at TIMESTAMPTZ NULL;

ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_email_key;
ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_username_key;

CREATE UNIQUE INDEX uq_app_user_username_active
    ON app_user (username)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_app_user_email_active
    ON app_user (email)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_app_user_deleted_at
    ON app_user (deleted_at);
