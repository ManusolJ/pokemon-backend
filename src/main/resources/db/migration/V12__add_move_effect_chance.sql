-- =============================================================================
-- V12__add_move_effect_chance.sql
-- Add effect_chance column to move for secondary effect probability
-- =============================================================================

ALTER TABLE move
    ADD COLUMN effect_chance INTEGER;