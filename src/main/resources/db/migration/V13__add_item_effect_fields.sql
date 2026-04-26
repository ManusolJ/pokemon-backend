-- =============================================================================
-- V13__add_item_effect_field.sql
-- Add effect column to item for mechanical text (from effectEntries),
-- separate from description which stores in-game flavor text
-- =============================================================================

ALTER TABLE item
    ADD COLUMN effect TEXT;