-- =============================================================================
-- V11__fix_entity_schema_mismatches.sql
-- Rename reserved `order` keyword, enforce NOT NULL constraints,
-- align seed_log status default with SeedStatus enum values
-- =============================================================================

ALTER TABLE pokemon         RENAME COLUMN "order" TO sort_order;
ALTER TABLE pokemon_species RENAME COLUMN "order" TO sort_order;

ALTER TABLE pokemon         ALTER COLUMN sort_order SET NOT NULL;
ALTER TABLE pokemon_species ALTER COLUMN sort_order SET NOT NULL;
ALTER TABLE pokemon_species ALTER COLUMN genus      SET NOT NULL;
ALTER TABLE move            ALTER COLUMN type_id    SET NOT NULL;

ALTER TABLE seed_log ALTER COLUMN status SET DEFAULT 'Running';