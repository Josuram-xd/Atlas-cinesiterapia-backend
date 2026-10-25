package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.comentario.service.ComentarioService;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.ratelimit.RateLimiter;
import co.edu.atlas.ejercicio.EjercicioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTests {

    @Mock
    ComentarioRepository comentarios;

    @Mock
    EjercicioRepository ejercicios;

    @Mock
    co.edu.atlas.auth.usuario.UsuarioRepository usuarios;

    @Mock
    RateLimiter limiter;

    @InjectMocks
    ComentarioService service;

    @Test
    void throttlesUsersWhoCommentTooFast() {
        when(limiter.tryAcquire("comentar|7")).thenReturn(false);

        assertThatThrownBy(() -> service.crear(1L, "hola", 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("DEMASIADOS_INTENTOS");
                    assertThat(e.getStatus().value()).isEqualTo(429);
                });
        verify(comentarios, never()).save(any());
    }

    @Test
    void rejectsMissingSheetBeforeSaving() {
        when(limiter.tryAcquire("comentar|7")).thenReturn(true);
        when(ejercicios.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(1L, "hola", 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("EJERCICIO_NO_ENCONTRADO"));
        verify(comentarios, never()).save(any());
    }
}
