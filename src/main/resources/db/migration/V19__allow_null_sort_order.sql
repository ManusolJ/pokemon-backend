-- =============================================================================
-- V19__allow_null_sort_order.sql
-- PokeAPI returns -1 for `order` on Pokemon and species that don't belong to
-- a canonical evolution-line position (alt forms, megas, gmax variants, etc.).
-- =============================================================================

ALTER TABLE pokemon         ALTER COLUMN sort_order DROP NOT NULL;
ALTER TABLE pokemon_species ALTER COLUMN sort_order DROP NOT NULL;

UPDATE pokemon         SET sort_order = NULL WHERE sort_order < 0;
UPDATE pokemon_species SET sort_order = NULL WHERE sort_order < 0;