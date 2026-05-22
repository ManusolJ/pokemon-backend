-- =============================================================================
-- V22__pokemon_default_form_index.sql
-- Composite index on (species_id, is_default_form). Supports the Pokédex filter
-- path: "for each species, locate its default form to evaluate Pokémon-level
-- predicates (types, base stats, height/weight)". Without this index, the
-- correlated EXISTS subqueries in SpeciesQueryService.addPokemonFilters and the
-- default-form lookup in fetchDefaultFormsBySpeciesId fall back to a sequential
-- scan over the pokemon table.
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_pokemon_species_default
    ON pokemon(species_id, is_default_form);
