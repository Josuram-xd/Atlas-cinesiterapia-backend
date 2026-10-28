package co.edu.atlas.metricas;

import co.edu.atlas.auth.usuario.Usuario;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Consulta de solo lectura: usuarios activos (con visita o sesion en el periodo) agrupados por rol de perfil. */
public interface UsuariosActivosRepository extends Repository<Usuario, Long> {

    /**
     * Filas [rolPerfil, total] de cuentas activas con al menos una visita en [{@code desde}, {@code hasta}] o una
     * sesion que se solape con [{@code inicio}, {@code finExclusivo}). Usa ix_visita_fecha e ix_sesion_ping.
     */
    @Query("select u.rolPerfil, count(u) from Usuario u where u.activo = true and ("
            + "u.id in (select v.usuarioId from Visita v where v.fecha between :desde and :hasta) "
            + "or u.id in (select s.usuarioId from SesionUso s "
            + "where s.ultimoPingAt >= :inicio and s.inicioAt < :finExclusivo)) group by u.rolPerfil")
    List<Object[]> contarActivosPorRol(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("inicio") Instant inicio,
            @Param("finExclusivo") Instant finExclusivo);
}
