-- =============================================================================
-- V25__case_insensitive_user_identifiers.sql
-- Makes username and email unique regardless of case among active (non-tombstoned)
-- rows. Until now `Alice` and `alice` could hold two separate accounts, and an
-- email lookup only matched the exact case the address was registered with.
--
-- Emails are lower-cased in place; the application now stores them that way.
-- Usernames keep the case the user chose and are compared case-insensitively.
-- =============================================================================

-- Refuse to run over data the new indexes would reject, with a message that says
-- what to fix, rather than failing on an anonymous unique violation.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM app_user WHERE deleted_at IS NULL
               GROUP BY lower(username) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'V25: active accounts share a username that differs only by case. Rename or tombstone one of each pair, then re-run.';
    END IF;

    IF EXISTS (SELECT 1 FROM app_user WHERE deleted_at IS NULL
               GROUP BY lower(email) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'V25: active accounts share an email that differs only by case. Change or tombstone one of each pair, then re-run.';
    END IF;
END $$;

UPDATE app_user SET email = lower(email) WHERE email <> lower(email);

DROP INDEX IF EXISTS uq_app_user_username_active;
DROP INDEX IF EXISTS uq_app_user_email_active;

CREATE UNIQUE INDEX uq_app_user_username_active_ci
    ON app_user (lower(username))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_app_user_email_active_ci
    ON app_user (lower(email))
    WHERE deleted_at IS NULL;
