package co.edu.atlas.comentario;

/** Ciclo de vida de un comentario. EN_REVISION solo lo ve su autor; ELIMINADO es borrado blando. */
public enum EstadoComentario {
    PUBLICADO,
    EN_REVISION,
    ELIMINADO
}
