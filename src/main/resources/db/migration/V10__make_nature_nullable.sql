-- V10__make_nature_nullable.sql
-- This migration makes the nature_id column in the team_pokemon table nullable, allowing for Pokémon without a specified nature.
ALTER TABLE team_pokemon ALTER COLUMN nature_id DROP NOT NULL;