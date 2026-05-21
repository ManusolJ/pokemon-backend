-- =============================================================================
-- V20__add_pokemon_artwork_shiny.sql
-- Adding the column so the frontend card can flip both sprite and artwork when shiny is on.
-- =============================================================================

ALTER TABLE pokemon ADD COLUMN artwork_shiny TEXT;
