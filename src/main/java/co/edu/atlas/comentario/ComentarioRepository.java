package co.edu.atlas.comentario;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    String VISIBLE_RAIZ = "c.ejercicioId = :ejercicioId and c.padreId is null and ("
            + "c.estado = co.edu.atlas.comentario.EstadoComentario.PUBLICADO "
            + "or (c.estado = co.edu.atlas.comentario.EstadoComentario.EN_REVISION and c.autor.id = :viewerId) "
            + "or (c.estado = co.edu.atlas.comentario.EstadoComentario.ELIMINADO and exists ("
            + "select r.id from Comentario r where r.padreId = c.id and ("
            + "r.estado = co.edu.atlas.comentario.EstadoComentario.PUBLICADO "
            + "or (r.estado = co.edu.atlas.comentario.EstadoComentario.EN_REVISION and r.autor.id = :viewerId)))))";

    /**
     * Raices visibles para {@code viewerId}: publicadas, las propias en revision y las eliminadas que conservan
     * respuestas visibles (se muestran como "comentario eliminado"). Mas recientes primero.
     */
    @Query(
            value = "select c from Comentario c where " + VISIBLE_RAIZ + " order by c.createdAt desc, c.id desc",
            countQuery = "select count(c) from Comentario c where " + VISIBLE_RAIZ)
    @EntityGraph(attributePaths = "autor")
    Page<Comentario> raicesVisibles(
            @Param("ejercicioId") Long ejercicioId, @Param("viewerId") Long viewerId, Pageable pageable);

    /** Respuestas visibles para {@code viewerId} de un lote de raices, en orden cronologico. */
    @Query("select c from Comentario c where c.padreId in :padres and ("
            + "c.estado = co.edu.atlas.comentario.EstadoComentario.PUBLICADO "
            + "or (c.estado = co.edu.atlas.comentario.EstadoComentario.EN_REVISION and c.autor.id = :viewerId)) "
            + "order by c.createdAt asc, c.id asc")
    @EntityGraph(attributePaths = "autor")
    List<Comentario> respuestasVisibles(@Param("padres") Collection<Long> padres, @Param("viewerId") Long viewerId);

    @Override
    @EntityGraph(attributePaths = "autor")
    Optional<Comentario> findById(Long id);
}
