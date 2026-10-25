package co.edu.atlas;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigTests {

    @Autowired
    DataSource dataSource;

    @Autowired
    Flyway flyway;

    @Test
    void hikariPoolIsSmall() {
        HikariDataSource hikari = (HikariDataSource) dataSource;
        assertThat(hikari.getMaximumPoolSize()).isLessThanOrEqualTo(5);
    }

    @Test
    void flywayAppliedBaselineMigration() {
        assertThat(flyway.info().applied()).isNotEmpty();
        assertThat(flyway.info().applied()[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void prodProfileRequiresSsl() throws IOException {
        List<PropertySource<?>> sources =
                new YamlPropertySourceLoader().load("prod", new ClassPathResource("application-prod.yml"));
        Object sslmode = sources.get(0).getProperty("spring.datasource.hikari.data-source-properties.sslmode");
        assertThat(sslmode).isEqualTo("require");
    }
}
