-- =============================================================================
-- V4__flatten_pokemon_forms_and_add_flags.sql
-- Merge pokemon_form into pokemon table, add classification flags
-- =============================================================================

ALTER TABLE pokemon
    ADD COLUMN form_name VARCHAR(50),
    ADD COLUMN base_species_id INTEGER REFERENCES pokemon(id),
    ADD COLUMN is_default_form BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE pokemon
    ADD COLUMN is_legendary BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN is_mythical BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN is_baby BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_pokemon_base_species ON pokemon(base_species_id);
CREATE INDEX idx_pokemon_default_form ON pokemon(is_default_form);

DROP TABLE pokemon_form;