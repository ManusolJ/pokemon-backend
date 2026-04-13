-- =============================================================================
-- V3__add_pokemon_evolution_fields.sql
-- Add self-referencing evolution data to pokemon table
-- =============================================================================

ALTER TABLE pokemon
    ADD COLUMN previous_evolution_id INTEGER REFERENCES pokemon(id),
    ADD COLUMN evolution_trigger VARCHAR(30),
    ADD COLUMN evolution_min_level INTEGER,
    ADD COLUMN evolution_item VARCHAR(50),
    ADD COLUMN evolution_held_item VARCHAR(50),
    ADD COLUMN evolution_min_happiness INTEGER,
    ADD COLUMN evolution_time_of_day VARCHAR(10);

CREATE INDEX idx_pokemon_previous_evolution ON pokemon(previous_evolution_id);