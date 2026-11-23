package co.edu.atlas.comentario.service;

import co.edu.atlas.comentario.dto.PalabraResponse;
import co.edu.atlas.comentario.filtro.NormalizadorOfensivo;
import co.edu.atlas.comentario.filtro.PalabraOfensiva;
import co.edu.atlas.comentario.filtro.PalabraOfensivaRepository;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.web.Pagina;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD administrativo de la lista de palabras ofensivas. El filtro lee la lista en cada analisis. */
@Service
@Transactional
public class PalabraOfensivaService {

    private static final int MIN_LETRAS = 3;

    private final PalabraOfensivaRepository palabras;

    public PalabraOfensivaService(PalabraOfensivaRepository palabras) {
        this.palabras = palabras;
    }

    @Transactional(readOnly = true)
    public Pagina<PalabraResponse> listar(String q, int page, int size) {
        PageRequest pagina = PageRequest.of(page, size, Sort.by("palabraNormalizada"));
        String filtro = q == null ? "" : NormalizadorOfensivo.normalizarPalabra(q);
        return Pagina.de(palabras.findByPalabraNormalizadaContaining(filtro, pagina), PalabraResponse::de);
    }

    public PalabraResponse crear(String palabra) {
        exigirValida(palabra);
        exigirUnica(NormalizadorOfensivo.normalizarPalabra(palabra), null);
        return PalabraResponse.de(guardar(new PalabraOfensiva(palabra)));
    }

    public PalabraResponse actualizar(Long id, String palabra) {
        PalabraOfensiva p = existente(id);
        exigirValida(palabra);
        exigirUnica(NormalizadorOfensivo.normalizarPalabra(palabra), id);
        p.cambiar(palabra);
        return PalabraResponse.de(guardar(p));
    }

    public void eliminar(Long id) {
        palabras.delete(existente(id));
    }

    private PalabraOfensiva guardar(PalabraOfensiva p) {
        try {
            return palabras.saveAndFlush(p);
        } catch (DataIntegrityViolationException e) {
            throw duplicada();
        }
    }

    private PalabraOfensiva existente(Long id) {
        return palabras
                .findById(id)
                .orElseThrow(() -> ApiException.notFound("PALABRA_NO_ENCONTRADA", "La palabra no existe"));
    }

    private static void exigirValida(String palabra) {
        if (NormalizadorOfensivo.normalizarPalabra(palabra).replace(" ", "").length() < MIN_LETRAS) {
            throw ApiException.badRequest(
                    "PALABRA_INVALIDA", "La palabra debe tener al menos " + MIN_LETRAS + " letras");
        }
    }

    private void exigirUnica(String normalizada, Long propioId) {
        palabras.findByPalabraNormalizada(normalizada).filter(p -> !p.getId().equals(propioId)).ifPresent(p -> {
            throw duplicada();
        });
    }

    private static ApiException duplicada() {
        return ApiException.conflict("PALABRA_DUPLICADA", "La palabra ya esta en la lista");
    }
}
