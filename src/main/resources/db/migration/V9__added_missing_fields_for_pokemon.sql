-- =============================================================================
-- V9__added_missing_fields_for_pokemon.sql
-- Sync pokemon and pokemon_species tables with entity fields
-- =============================================================================

ALTER TABLE pokemon
    ADD COLUMN "order" INTEGER;

ALTER TABLE pokemon_species
    ADD COLUMN "order"       INTEGER,
    ADD COLUMN genus         VARCHAR(50),
    ADD COLUMN hatch_counter INTEGER;