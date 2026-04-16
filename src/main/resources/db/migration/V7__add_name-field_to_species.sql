-- =============================================================================
-- V7__add_species_name.sql
-- Add name column to pokemon_species so Pokédex listings don't require
-- a join to the pokemon table
-- =============================================================================

ALTER TABLE pokemon_species
    ADD COLUMN name VARCHAR(50) NOT NULL;

ALTER TABLE pokemon_species
    ADD CONSTRAINT uq_species_name UNIQUE (name);

CREATE INDEX idx_species_name ON pokemon_species(name);