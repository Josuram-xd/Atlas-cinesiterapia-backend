package co.edu.atlas.media.dto;

import java.time.Instant;
import java.util.Map;

public record SubidaResponse(Long mediaId, String urlSubida, String metodo, Map<String, String> cabeceras, Instant expiraEn) {}
