package co.edu.atlas.comentario;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReporteComentarioRepository extends JpaRepository<ReporteComentario, Long> {

    boolean existsByComentarioIdAndUsuarioId(Long comentarioId, Long usuarioId);

    long countByComentarioId(Long comentarioId);

    /** Filas [comentarioId, total] para un lote de comentarios (evita N+1 en la cola de revision). */
    @Query("select r.comentarioId, count(r) from ReporteComentario r where r.comentarioId in :ids group by r.comentarioId")
    List<Object[]> contarPorComentario(@Param("ids") Collection<Long> ids);

    @Modifying
    @Query("delete from ReporteComentario r where r.comentarioId = :comentarioId")
    int borrarDeComentario(@Param("comentarioId") Long comentarioId);
}
