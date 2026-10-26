package co.edu.atlas.media;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Almacenamiento en memoria para desarrollo y tests: no hace llamadas de red. Asume que el cliente subio
 * el archivo apenas se emite la URL, de modo que la confirmacion funciona sin R2.
 */
@Component
@ConditionalOnProperty(prefix = "app.media", name = "proveedor", havingValue = "fake", matchIfMissing = true)
public class AlmacenamientoFake implements AlmacenamientoMedia {

    private static final String BASE = "https://almacenamiento-fake.invalid/";

    private final Map<String, ObjetoAlmacenado> objetos = new ConcurrentHashMap<>();
    private final MediaProperties props;
    private final Clock clock;

    public AlmacenamientoFake(MediaProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    @Override
    public SubidaPrefirmada prepararSubida(String clave, String contentType, long tamanoBytes) {
        objetos.put(clave, new ObjetoAlmacenado(tamanoBytes, contentType));
        return new SubidaPrefirmada(
                BASE + clave + "?firma=fake", "PUT", Map.of("Content-Type", contentType),
                clock.instant().plus(props.subidaTtl()));
    }

    @Override
    public String urlLectura(String clave) {
        return BASE + clave;
    }

    @Override
    public Optional<ObjetoAlmacenado> informacion(String clave) {
        return Optional.ofNullable(objetos.get(clave));
    }

    @Override
    public void eliminar(String clave) {
        objetos.remove(clave);
    }

    /** Solo tests: simula que el archivo subido pesa distinto de lo declarado. */
    public void simularTamanoReal(String clave, long tamanoBytes) {
        ObjetoAlmacenado actual = objetos.get(clave);
        objetos.put(clave, new ObjetoAlmacenado(tamanoBytes, actual == null ? null : actual.contentType()));
    }
}
