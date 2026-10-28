package co.edu.atlas.comentario.dto;

import co.edu.atlas.comentario.filtro.PalabraOfensiva;
import java.time.Instant;

public record PalabraResponse(Long id, String palabra, Instant createdAt) {

    public static PalabraResponse de(PalabraOfensiva p) {
        return new PalabraResponse(p.getId(), p.getPalabra(), p.getCreatedAt());
    }
}
