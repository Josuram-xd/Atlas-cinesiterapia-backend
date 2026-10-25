package co.edu.atlas.auth.security;

import co.edu.atlas.auth.usuario.RolSistema;

/** Principal autenticado extraido del access token. */
public record AuthenticatedUser(Long id, String correo, RolSistema rol) {}
