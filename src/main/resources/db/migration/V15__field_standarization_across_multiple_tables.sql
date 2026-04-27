-- =============================================================================
-- V15__field_standarization_across_multiple_tables.sql
-- Standardize field names across multiple tables for consistency and clarity.
-- =============================================================================

ALTER TABLE ability
    ADD COLUMN short_effect TEXT,
    ADD COLUMN flavor_text TEXT;

ALTER TABLE ability
    DROP COLUMN description;

ALTER TABLE item
    ADD COLUMN short_effect TEXT,
    ADD COLUMN flavor_text TEXT;

ALTER TABLE item
    DROP COLUMN description;

ALTER TABLE move
    ADD COLUMN short_effect TEXT,
    ADD COLUMN effect TEXT;

ALTER TABLE move
    DROP COLUMN effect_description;