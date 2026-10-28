package co.edu.atlas.comentario.filtro;

import co.edu.atlas.comentario.EstadoComentario;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Filtro automatico de contenido ofensivo (HU-24). La lista se lee de la base en cada analisis: es pequena y asi
 * los cambios del administrador aplican de inmediato sin cache que invalidar.
 */
@Service
@Transactional(readOnly = true)
public class FiltroOfensivoService {

    private final PalabraOfensivaRepository palabras;

    public FiltroOfensivoService(PalabraOfensivaRepository palabras) {
        this.palabras = palabras;
    }

    public boolean esOfensivo(String texto) {
        Set<String> lista = new HashSet<>(palabras.todasNormalizadas());
        return NormalizadorOfensivo.contieneOfensiva(texto, lista);
    }

    /** Estado con el que debe quedar un texto recien enviado o editado. */
    public EstadoComentario estadoPara(String texto) {
        return esOfensivo(texto) ? EstadoComentario.EN_REVISION : EstadoComentario.PUBLICADO;
    }
}
