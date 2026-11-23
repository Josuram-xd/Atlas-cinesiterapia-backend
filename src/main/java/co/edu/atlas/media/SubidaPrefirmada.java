package co.edu.atlas.media;

import java.time.Instant;
import java.util.Map;

/** Instrucciones para que el cliente suba el archivo directamente al almacenamiento. */
public record SubidaPrefirmada(String url, String metodo, Map<String, String> cabeceras, Instant expiraEn) {}
