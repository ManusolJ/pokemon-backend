-- =============================================================================
-- V14__add_move_flavortext_field.sql
-- Add flavor_text column to move for in-game flavor text,
-- separate from effect_description which stores mechanical text
-- =============================================================================

ALTER TABLE move
    ADD COLUMN flavor_text TEXT;