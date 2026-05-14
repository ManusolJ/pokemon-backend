-- =============================================================================
-- V19__allow_null_sort_order.sql
-- PokeAPI returns -1 for `order` on Pokemon and species that don't belong to
-- a canonical evolution-line position (alt forms, megas, gmax variants, etc.).
-- Store NULL instead so they fall to the end of ASC sorts on `sort_order`
-- (Postgres orders NULLs last on ASC by default).
-- =============================================================================

UPDATE pokemon         SET sort_order = NULL WHERE sort_order < 0;
UPDATE pokemon_species SET sort_order = NULL WHERE sort_order < 0;

ALTER TABLE pokemon         ALTER COLUMN sort_order DROP NOT NULL;
ALTER TABLE pokemon_species ALTER COLUMN sort_order DROP NOT NULL;