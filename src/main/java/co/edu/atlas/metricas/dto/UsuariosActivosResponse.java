package co.edu.atlas.metricas.dto;

import co.edu.atlas.auth.usuario.RolPerfil;
import java.time.LocalDate;
import java.util.List;

/** Usuarios activos del periodo por rol de perfil: siempre incluye los 4 roles, con cero si no hubo actividad. */
public record UsuariosActivosResponse(LocalDate desde, LocalDate hasta, long total, List<PorRol> porRol) {

    public record PorRol(RolPerfil rol, long usuarios) {}
}
