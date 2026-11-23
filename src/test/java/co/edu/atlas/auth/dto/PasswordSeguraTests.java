package co.edu.atlas.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordSeguraTests {

    private final PasswordSegura.Validador validador = new PasswordSegura.Validador();

    @Test
    void acceptsLongPasswordWithLettersAndDigits() {
        assertThat(validador.isValid("Narnen13mayo.", null)).isTrue();
        assertThat(validador.isValid("rodilla2026x", null)).isTrue();
    }

    @Test
    void rejectsShortOnlyLettersOnlyDigitsAndCommon() {
        assertThat(validador.isValid("abc12345", null)).isFalse(); // corta
        assertThat(validador.isValid("soloLetrasAqui", null)).isFalse();
        assertThat(validador.isValid("12345678901", null)).isFalse();
        assertThat(validador.isValid("Password123", null)).isFalse(); // comun
    }
}
