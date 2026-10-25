-- Busqueda parcial por nombre (LIKE '%texto%') acelerada con trigramas. Solo PostgreSQL (Neon permite pg_trgm).
-- nombre_busqueda ya viene en minusculas y sin tildes desde la aplicacion.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX ix_ejercicio_nombre_trgm ON ejercicio USING gin (nombre_busqueda gin_trgm_ops);
