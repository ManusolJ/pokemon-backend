-- =============================================================================
-- V5__extract_pokemon_species.sql
-- Split pokemon into pokemon (form-level) and pokemon_species (species-level)
-- Add egg_group support and Pokédex enrichment fields
-- =============================================================================

TRUNCATE TABLE
    team_pokemon_move,
    team_pokemon,
    pokemon_move,
    pokemon_ability,
    pokemon
CASCADE;

CREATE TABLE pokemon_species (
    id                      INTEGER PRIMARY KEY,
    national_dex_number     INTEGER NOT NULL,
    gender_rate             INTEGER,
    flavor_text             TEXT,
    generation              INTEGER,
    catch_rate              INTEGER,
    base_happiness          INTEGER,
    growth_rate             VARCHAR(30),
    is_legendary            BOOLEAN NOT NULL DEFAULT FALSE,
    is_mythical             BOOLEAN NOT NULL DEFAULT FALSE,
    is_baby                 BOOLEAN NOT NULL DEFAULT FALSE,
    previous_evolution_id   INTEGER REFERENCES pokemon_species(id),
    evolution_trigger       VARCHAR(30),
    evolution_min_level     INTEGER,
    evolution_item          VARCHAR(50),
    evolution_held_item     VARCHAR(50),
    evolution_min_happiness INTEGER,
    evolution_time_of_day   VARCHAR(10)
);

CREATE INDEX idx_species_dex_number ON pokemon_species(national_dex_number);
CREATE INDEX idx_species_generation ON pokemon_species(generation);
CREATE INDEX idx_species_prev_evo   ON pokemon_species(previous_evolution_id);

CREATE TABLE egg_group (
    id   INTEGER PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE pokemon_species_egg_group (
    species_id   INTEGER NOT NULL REFERENCES pokemon_species(id),
    egg_group_id INTEGER NOT NULL REFERENCES egg_group(id),
    PRIMARY KEY (species_id, egg_group_id)
);

DROP INDEX IF EXISTS idx_pokemon_national_dex;
DROP INDEX IF EXISTS idx_pokemon_base_species;
DROP INDEX IF EXISTS idx_pokemon_default_form;
DROP INDEX IF EXISTS idx_pokemon_previous_evolution;

ALTER TABLE pokemon
    DROP COLUMN national_dex_number,
    DROP COLUMN gender_rate,
    DROP COLUMN flavor_text,
    DROP COLUMN generation,
    DROP COLUMN is_legendary,
    DROP COLUMN is_mythical,
    DROP COLUMN is_baby,
    DROP COLUMN previous_evolution_id,
    DROP COLUMN evolution_trigger,
    DROP COLUMN evolution_min_level,
    DROP COLUMN evolution_item,
    DROP COLUMN evolution_held_item,
    DROP COLUMN evolution_min_happiness,
    DROP COLUMN evolution_time_of_day,
    DROP COLUMN base_species_id;

ALTER TABLE pokemon
    ADD COLUMN species_id INTEGER NOT NULL REFERENCES pokemon_species(id);

CREATE INDEX idx_pokemon_species ON pokemon(species_id);