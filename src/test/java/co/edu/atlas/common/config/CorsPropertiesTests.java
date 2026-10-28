package co.edu.atlas.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class CorsPropertiesTests {

    @Test
    void acceptsExactOriginsAndTrimsThem() {
        CorsProperties p = new CorsProperties(List.of(" https://atlas.example.com ", "http://localhost:5173"));
        assertThat(p.allowedOrigins()).containsExactly("https://atlas.example.com", "http://localhost:5173");
    }

    @Test
    void rejectsWildcard() {
        assertThatThrownBy(() -> new CorsProperties(List.of("*"))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new CorsProperties(List.of("https://*.example.com")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsOriginsWithPathOrTrailingSlashOrNoScheme() {
        assertThatThrownBy(() -> new CorsProperties(List.of("https://atlas.example.com/")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new CorsProperties(List.of("atlas.example.com")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyIsAllowedAndMeansNoCrossOrigin() {
        assertThat(new CorsProperties(null).allowedOrigins()).isEmpty();
        assertThat(new CorsProperties(List.of("", " ")).allowedOrigins()).isEmpty();
    }
}
