-- =============================================================================
-- V16__learn_method_field_change.sql 
-- Change the data type of the learn_method field in the pokemon_move table to VARCHAR(50) for better readability and consistency.
-- =============================================================================

ALTER TABLE pokemon_move ALTER COLUMN learn_method TYPE VARCHAR(50);