-- =============================================================================
-- V2__drop_evolution_chain.sql
-- Remove evolution_chain table and its FK from pokemon
-- =============================================================================

ALTER TABLE pokemon DROP COLUMN evolution_chain_id;

DROP TABLE evolution_chain;