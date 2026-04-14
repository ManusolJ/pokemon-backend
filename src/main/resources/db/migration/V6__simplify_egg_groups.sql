-- =============================================================================
-- V6__simplify_egg_groups.sql
-- Replace egg_group + pokemon_species_egg_group tables with two simple
-- VARCHAR columns on pokemon_species (a species has at most 2 egg groups)
-- =============================================================================

DROP TABLE pokemon_species_egg_group;
DROP TABLE egg_group;

ALTER TABLE pokemon_species
    ADD COLUMN egg_group_1 VARCHAR(30),
    ADD COLUMN egg_group_2 VARCHAR(30);