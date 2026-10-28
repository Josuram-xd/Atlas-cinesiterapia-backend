package co.edu.atlas.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.common.error.ApiException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimiterTests {

    private final MutableClock clock = new MutableClock();
    private final RateLimiter limiter = new RateLimiter(new RateLimitProperties(2, Duration.ofMinutes(1)), clock);

    @Test
    void allowsUpToMaxThenBlocks() {
        assertThat(limiter.tryAcquire("a")).isTrue();
        assertThat(limiter.tryAcquire("a")).isTrue();
        assertThat(limiter.tryAcquire("a")).isFalse();
    }

    @Test
    void keysAreIndependent() {
        limiter.tryAcquire("a");
        limiter.tryAcquire("a");

        assertThat(limiter.tryAcquire("b")).isTrue();
    }

    @Test
    void windowSlidesAndAllowsAgain() {
        limiter.tryAcquire("a");
        limiter.tryAcquire("a");
        clock.advance(Duration.ofSeconds(61));

        assertThat(limiter.tryAcquire("a")).isTrue();
    }

    @Test
    void blockedAttemptsDoNotExtendTheBlock() {
        limiter.tryAcquire("a");
        limiter.tryAcquire("a");
        clock.advance(Duration.ofSeconds(30));
        limiter.tryAcquire("a");
        clock.advance(Duration.ofSeconds(31));

        assertThat(limiter.tryAcquire("a")).isTrue();
    }

    @Test
    void exigirThrows429OnceTheLimitIsReached() {
        assertThatCode(() -> limiter.exigir("a", "espera")).doesNotThrowAnyException();
        limiter.exigir("a", "espera");

        assertThatThrownBy(() -> limiter.exigir("a", "espera"))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(429);
                    assertThat(e.getCode()).isEqualTo("DEMASIADOS_INTENTOS");
                    assertThat(e.getMessage()).isEqualTo("espera");
                });
    }

    @Test
    void retryAfterMatchesWindow() {
        assertThat(limiter.retryAfterSeconds()).isEqualTo(60);
    }
}
