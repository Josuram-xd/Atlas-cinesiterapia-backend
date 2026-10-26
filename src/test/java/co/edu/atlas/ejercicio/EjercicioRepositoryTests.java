package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.catalogo.CategoriaTerapeuticaRepository;
import co.edu.atlas.catalogo.RegionAnatomicaRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EjercicioRepositoryTests {

    @Autowired
    EjercicioRepository ejercicios;

    @Autowired
    RegionAnatomicaRepository regiones;

    @Autowired
    CategoriaTerapeuticaRepository categorias;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    EntityManager em;

    private Ejercicio guardar(String nombre) {
        Usuario admin = usuarios.save(new Usuario(nombre.hashCode() + "@x.com", "hash", RolPerfil.PROFESIONAL, RolSistema.ADMIN));
        Ejercicio e = new Ejercicio(
                nombre,
                regiones.findBySvgId("anterior-rodilla").orElseThrow(),
                categorias.findAllByOrderByOrdenAsc().get(0),
                admin.getId());
        return ejercicios.saveAndFlush(e);
    }

    @Test
    void persistsAndReloadsAllContent() {
        Ejercicio e = guardar("Flexión");
        e.actualizar(new ContenidoFicha(
                "Flexión", e.getRegion(), e.getCategoria(), "obj", "est", "ind", "prec", "pac", "fis", "eje", "cmd",
                new Dosificacion("3", "12", "media", "30 s", "60 s", "diaria"), "prog", "err",
                List.of("Ana", "Luis"), "2025-1", "Dra. Pérez"), e.getCreadoPor());
        ejercicios.saveAndFlush(e);
        em.clear();

        Ejercicio r = ejercicios.findById(e.getId()).orElseThrow();

        assertThat(r.getObjetivoTerapeutico()).isEqualTo("obj");
        assertThat(r.getDosificacion().getFrecuencia()).isEqualTo("diaria");
        assertThat(r.getEstudiantesCreadores()).containsExactly("Ana", "Luis");
        assertThat(r.getCohorte()).isEqualTo("2025-1");
        assertThat(r.getEstado()).isEqualTo(EstadoEjercicio.BORRADOR);
        assertThat(r.getCreatedAt()).isNotNull();
    }

    @Test
    void softDeletedRowsAreInvisibleToQueries() {
        Ejercicio e = guardar("Borrar");
        e.eliminar(Instant.now());
        ejercicios.saveAndFlush(e);
        em.clear();

        assertThat(ejercicios.findById(e.getId())).isEmpty();
        assertThat(ejercicios.findAll()).noneMatch(x -> x.getId().equals(e.getId()));
    }
}
