package co.edu.atlas.common.ratelimit;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Ventana deslizante en memoria. Suficiente para una sola instancia (plan free); no es distribuido. */
@Component
public class RateLimiter {

    private static final int PURGE_THRESHOLD = 10_000;

    private final RateLimitProperties props;
    private final Clock clock;
    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    public RateLimiter(RateLimitProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    /** Registra el intento y devuelve false si el cliente ya supero el limite en la ventana actual. */
    public boolean tryAcquire(String key) {
        Instant now = clock.instant();
        Instant desde = now.minus(props.window());
        if (hits.size() > PURGE_THRESHOLD) {
            purgar(desde);
        }
        Deque<Instant> cola = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (cola) {
            while (!cola.isEmpty() && !cola.peekFirst().isAfter(desde)) {
                cola.pollFirst();
            }
            if (cola.size() >= props.maxRequests()) {
                return false;
            }
            cola.addLast(now);
            return true;
        }
    }

    /** Segundos hasta que el cliente pueda reintentar (minimo 1). */
    public long retryAfterSeconds() {
        return Math.max(1, props.window().toSeconds());
    }

    private void purgar(Instant desde) {
        hits.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || !e.getValue().peekLast().isAfter(desde);
            }
        });
    }
}
