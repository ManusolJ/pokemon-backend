-- =============================================================================
-- V1__initial_schema.sql
-- Initial database schema
-- =============================================================================

CREATE TABLE type (
    id INTEGER PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE type_effectiveness (
    attacking_type_id INTEGER NOT NULL REFERENCES type(id),
    defending_type_id INTEGER NOT NULL REFERENCES type(id),
    multiplier NUMERIC(3, 2) NOT NULL,
    PRIMARY KEY (attacking_type_id, defending_type_id)
);

CREATE TABLE nature (
    id INTEGER PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE,
    increased_stat VARCHAR(20),
    decreased_stat VARCHAR(20)
    constraint chk_nature_stats CHECK (
        (increased_stat IS NULL AND decreased_stat IS NULL) OR
        (increased_stat IS NOT NULL AND decreased_stat IS NOT NULL AND increased_stat <> decreased_stat)
    )
);

CREATE TABLE ability (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    effect TEXT
);

CREATE TABLE item (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50),
    sprite_url TEXT
);

CREATE TABLE move (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    type_id INTEGER REFERENCES type(id),
    category VARCHAR(10) NOT NULL CHECK (category IN ('physical', 'special', 'status')),
    pp INTEGER,
    power INTEGER,
    accuracy INTEGER,
    effect_description TEXT,
    priority INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE evolution_chain (
    id INTEGER PRIMARY KEY,
    chain_data JSONB NOT NULL
);

CREATE TABLE pokemon (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    national_dex_number INTEGER NOT NULL,
    primary_type_id INTEGER NOT NULL REFERENCES type(id),
    secondary_type_id INTEGER REFERENCES type(id),
    base_hp INTEGER NOT NULL,
    base_atk INTEGER NOT NULL,
    base_def INTEGER NOT NULL,
    base_sp_atk INTEGER NOT NULL,
    base_sp_def INTEGER NOT NULL,
    base_speed INTEGER NOT NULL,
    height INTEGER,
    weight INTEGER,
    gender_rate INTEGER,
    flavor_text TEXT,
    generation INTEGER,
    evolution_chain_id  INTEGER REFERENCES evolution_chain(id),
    sprite_default TEXT,
    sprite_shiny TEXT,
    artwork_url TEXT
);

CREATE TABLE pokemon_ability (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    ability_id INTEGER NOT NULL REFERENCES ability(id),
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    slot INTEGER NOT NULL,
    PRIMARY KEY (pokemon_id, ability_id)
);

CREATE TABLE pokemon_move (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    move_id INTEGER NOT NULL REFERENCES move(id),
    learn_method VARCHAR(20) NOT NULL,
    level_learned_at INTEGER,
    PRIMARY KEY (pokemon_id, move_id, learn_method)
);

CREATE TABLE pokemon_form (
    id INTEGER PRIMARY KEY,
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    form_name VARCHAR(50) NOT NULL,
    primary_type_id INTEGER REFERENCES type(id),
    secondary_type_id INTEGER REFERENCES type(id),
    base_hp INTEGER,
    base_atk INTEGER,
    base_def INTEGER,
    base_sp_atk INTEGER,
    base_sp_def INTEGER,
    base_speed INTEGER
);

CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE team (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    share_slug VARCHAR(36) UNIQUE,
    like_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE team_like (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    team_id BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, team_id)
);

CREATE TABLE team_pokemon (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    slot_position INTEGER NOT NULL CHECK (slot_position BETWEEN 1 AND 6),
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    nickname VARCHAR(12),
    level INTEGER NOT NULL DEFAULT 100 CHECK (level BETWEEN 1 AND 100),
    gender VARCHAR(10),
    is_shiny BOOLEAN NOT NULL DEFAULT FALSE,
    nature_id INTEGER NOT NULL REFERENCES nature(id),
    ability_id INTEGER NOT NULL REFERENCES ability(id),
    item_id INTEGER REFERENCES item(id),
    tera_type INTEGER REFERENCES type(id),
    ev_hp INTEGER NOT NULL DEFAULT 0 CHECK (ev_hp BETWEEN 0 AND 252),
    ev_atk INTEGER NOT NULL DEFAULT 0 CHECK (ev_atk BETWEEN 0 AND 252),
    ev_def INTEGER NOT NULL DEFAULT 0 CHECK (ev_def BETWEEN 0 AND 252),
    ev_sp_atk INTEGER NOT NULL DEFAULT 0 CHECK (ev_sp_atk BETWEEN 0 AND 252),
    ev_sp_def INTEGER NOT NULL DEFAULT 0 CHECK (ev_sp_def BETWEEN 0 AND 252),
    ev_speed INTEGER NOT NULL DEFAULT 0 CHECK (ev_speed BETWEEN 0 AND 252),
    iv_hp INTEGER NOT NULL DEFAULT 31 CHECK (iv_hp BETWEEN 0 AND 31),
    iv_atk INTEGER NOT NULL DEFAULT 31 CHECK (iv_atk BETWEEN 0 AND 31),
    iv_def INTEGER NOT NULL DEFAULT 31 CHECK (iv_def BETWEEN 0 AND 31),
    iv_sp_atk INTEGER NOT NULL DEFAULT 31 CHECK (iv_sp_atk BETWEEN 0 AND 31),
    iv_sp_def INTEGER NOT NULL DEFAULT 31 CHECK (iv_sp_def BETWEEN 0 AND 31),
    iv_speed INTEGER NOT NULL DEFAULT 31 CHECK (iv_speed BETWEEN 0 AND 31),
    CONSTRAINT uq_team_slot UNIQUE (team_id, slot_position),
    CONSTRAINT chk_ev_total CHECK (
        ev_hp + ev_atk + ev_def + ev_sp_atk + ev_sp_def + ev_speed <= 510
    )
);

CREATE TABLE team_pokemon_move (
    team_pokemon_id BIGINT NOT NULL REFERENCES team_pokemon(id) ON DELETE CASCADE,
    slot_position INTEGER NOT NULL CHECK (slot_position BETWEEN 1 AND 4),
    move_id INTEGER NOT NULL REFERENCES move(id),
    PRIMARY KEY (team_pokemon_id, slot_position)
);

CREATE TABLE seed_log (
    id BIGSERIAL PRIMARY KEY,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    entries_added INTEGER DEFAULT 0,
    errors INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    triggered_by VARCHAR(50)
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(30) NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity VARCHAR(50),
    entity_id VARCHAR(50),
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_date ON audit_log(created_at);

CREATE INDEX idx_team_user ON team(user_id);

CREATE INDEX idx_pokemon_name ON pokemon(name);
CREATE INDEX idx_pokemon_form_pokemon ON pokemon_form(pokemon_id);
CREATE INDEX idx_pokemon_national_dex ON pokemon(national_dex_number);

CREATE INDEX idx_move_name ON move(name);
CREATE INDEX idx_move_type ON move(type_id);
CREATE INDEX idx_pokemon_move_move ON pokemon_move(move_id);

CREATE INDEX idx_item_name ON item(name);
