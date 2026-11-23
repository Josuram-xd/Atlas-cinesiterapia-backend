package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** La migracion del indice de busqueda es especifica por motor (Flyway {vendor}). */
@SpringBootTest
@ActiveProfiles("test")
class IndiceBusquedaTests {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void h2GetsAPlainIndexOnTheNormalizedName() {
        List<String> indices = jdbc.queryForList(
                "SELECT LOWER(INDEX_NAME) FROM INFORMATION_SCHEMA.INDEXES WHERE LOWER(TABLE_NAME) = 'ejercicio'", String.class);

        assertThat(indices).contains("ix_ejercicio_nombre_busqueda");
    }

    @Test
    void postgresMigrationCreatesTheTrigramIndex() throws Exception {
        String sql = new ClassPathResource("db/vendor/postgresql/V9__busqueda_trgm.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(sql)
                .contains("CREATE EXTENSION IF NOT EXISTS pg_trgm")
                .contains("USING gin (nombre_busqueda gin_trgm_ops)");
    }

    @Test
    void bothVendorsDefineTheSameMigrationVersion() {
        assertThat(new ClassPathResource("db/vendor/h2/V9__busqueda_indice.sql").exists()).isTrue();
        assertThat(new ClassPathResource("db/vendor/postgresql/V9__busqueda_trgm.sql").exists()).isTrue();
    }
}
