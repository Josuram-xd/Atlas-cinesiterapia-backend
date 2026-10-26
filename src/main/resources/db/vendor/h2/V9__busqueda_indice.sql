-- H2 (solo tests): sin pg_trgm, la busqueda usa LIKE normalizado con un indice simple.
CREATE INDEX ix_ejercicio_nombre_busqueda ON ejercicio (nombre_busqueda);
