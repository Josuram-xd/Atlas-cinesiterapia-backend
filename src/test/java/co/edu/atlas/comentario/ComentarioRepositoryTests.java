package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.catalogo.CategoriaTerapeuticaRepository;
import co.edu.atlas.catalogo.RegionAnatomicaRepository;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ComentarioRepositoryTests {

    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    @Autowired
    ComentarioRepository comentarios;

    @Autowired
    ReporteComentarioRepository reportes;

    @Autowired
    EjercicioRepository ejercicios;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    RegionAnatomicaRepository regiones;

    @Autowired
    CategoriaTerapeuticaRepository categorias;

    Usuario ana;
    Usuario luis;
    Long ejercicioId;

    @BeforeEach
    void preparar() {
        ana = usuario();
        luis = usuario();
        ejercicioId = ejercicios
                .saveAndFlush(new Ejercicio(
                        "Ficha", regiones.findBySvgId("anterior-rodilla").orElseThrow(),
                        categorias.findAllByOrderByOrdenAsc().get(0), ana.getId()))
                .getId();
    }

    private Usuario usuario() {
        return usuarios.save(new Usuario("u" + SECUENCIA.incrementAndGet() + "@correo.com", "hash", RolPerfil.ESTUDIANTE, RolSistema.USER));
    }

    private Comentario guardar(Usuario autor, Long padre, String texto, EstadoComentario estado) {
        return comentarios.saveAndFlush(new Comentario(ejercicioId, autor, padre, texto, estado));
    }

    @Test
    void persistsCommentWithTimestampsAndDefaults() {
        Comentario c = guardar(ana, null, "Muy util", EstadoComentario.PUBLICADO);

        assertThat(c.getId()).isNotNull();
        assertThat(c.getCreatedAt()).isNotNull();
        assertThat(c.esRespuesta()).isFalse();
        assertThat(c.getEditadoAt()).isNull();
    }

    @Test
    void rootsListNewestFirstAndHideOthersPendingReview() {
        Comentario vieja = guardar(ana, null, "vieja", EstadoComentario.PUBLICADO);
        Comentario nueva = guardar(ana, null, "nueva", EstadoComentario.PUBLICADO);
        guardar(ana, null, "oculta de ana", EstadoComentario.EN_REVISION);

        List<Comentario> paraLuis = comentarios.raicesVisibles(ejercicioId, luis.getId(), PageRequest.of(0, 10)).getContent();
        List<Comentario> paraAna = comentarios.raicesVisibles(ejercicioId, ana.getId(), PageRequest.of(0, 10)).getContent();

        assertThat(paraLuis).extracting(Comentario::getId).containsExactly(nueva.getId(), vieja.getId());
        assertThat(paraAna).hasSize(3);
    }

    @Test
    void deletedRootAppearsOnlyWhileItHasVisibleReplies() {
        Comentario raiz = guardar(ana, null, "x", EstadoComentario.PUBLICADO);
        raiz.eliminar(Instant.now());
        comentarios.saveAndFlush(raiz);
        assertThat(comentarios.raicesVisibles(ejercicioId, luis.getId(), PageRequest.of(0, 10)).getTotalElements())
                .isZero();

        guardar(luis, raiz.getId(), "respuesta", EstadoComentario.PUBLICADO);

        assertThat(comentarios.raicesVisibles(ejercicioId, luis.getId(), PageRequest.of(0, 10)).getContent())
                .extracting(Comentario::getId)
                .containsExactly(raiz.getId());
    }

    @Test
    void repliesAreChronologicalAndExcludeDeletedAndForeignPendingOnes() {
        Comentario raiz = guardar(ana, null, "raiz", EstadoComentario.PUBLICADO);
        Comentario r1 = guardar(luis, raiz.getId(), "r1", EstadoComentario.PUBLICADO);
        Comentario r2 = guardar(ana, raiz.getId(), "r2", EstadoComentario.PUBLICADO);
        guardar(ana, raiz.getId(), "oculta", EstadoComentario.EN_REVISION);
        Comentario borrada = guardar(luis, raiz.getId(), "b", EstadoComentario.PUBLICADO);
        borrada.eliminar(Instant.now());
        comentarios.saveAndFlush(borrada);

        List<Comentario> paraLuis = comentarios.respuestasVisibles(Set.of(raiz.getId()), luis.getId());

        assertThat(paraLuis).extracting(Comentario::getId).containsExactly(r1.getId(), r2.getId());
    }

    @Test
    void softDeleteBlanksTextAndRejectsSecondDeletion() {
        Comentario c = guardar(ana, null, "secreto", EstadoComentario.PUBLICADO);

        c.eliminar(Instant.now());

        assertThat(c.getEstado()).isEqualTo(EstadoComentario.ELIMINADO);
        assertThat(c.getTexto()).isEmpty();
        assertThat(c.getDeletedAt()).isNotNull();
        assertThatThrownBy(() -> c.eliminar(Instant.now())).isInstanceOf(ApiException.class);
        assertThatThrownBy(c::aceptar).isInstanceOf(ApiException.class);
    }

    @Test
    void visibilityDependsOnStateAndAuthor() {
        Comentario oculto = guardar(ana, null, "x", EstadoComentario.EN_REVISION);

        assertThat(oculto.visiblePara(ana.getId())).isTrue();
        assertThat(oculto.visiblePara(luis.getId())).isFalse();
        oculto.aceptar();
        assertThat(oculto.visiblePara(luis.getId())).isTrue();
    }

    @Test
    void reportIsUniquePerUserAndComment() {
        Comentario c = guardar(ana, null, "x", EstadoComentario.PUBLICADO);
        reportes.saveAndFlush(new ReporteComentario(c.getId(), luis.getId()));

        assertThat(reportes.existsByComentarioIdAndUsuarioId(c.getId(), luis.getId())).isTrue();
        assertThatThrownBy(() -> reportes.saveAndFlush(new ReporteComentario(c.getId(), luis.getId())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void countsReportsInBatchAndClearsThem() {
        Comentario a = guardar(ana, null, "a", EstadoComentario.PUBLICADO);
        Comentario b = guardar(ana, null, "b", EstadoComentario.PUBLICADO);
        reportes.save(new ReporteComentario(a.getId(), luis.getId()));
        reportes.save(new ReporteComentario(a.getId(), usuario().getId()));
        reportes.saveAndFlush(new ReporteComentario(b.getId(), luis.getId()));

        List<Object[]> filas = reportes.contarPorComentario(Set.of(a.getId(), b.getId()));

        assertThat(filas).hasSize(2);
        assertThat(reportes.countByComentarioId(a.getId())).isEqualTo(2);
        assertThat(reportes.borrarDeComentario(a.getId())).isEqualTo(2);
        assertThat(reportes.countByComentarioId(a.getId())).isZero();
    }
}
