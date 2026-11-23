package co.edu.atlas.auth.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.Set;

/** Contrasena de 10+ caracteres con al menos una letra y un numero, y fuera de la lista de claves comunes. */
@Documented
@Constraint(validatedBy = PasswordSegura.Validador.class)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordSegura {
    int MIN = 10;

    String message() default
            "La contrasena debe tener al menos 10 caracteres, con letras y numeros, y no ser una clave comun";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<PasswordSegura, String> {
        // shortcut: lista corta de claves frecuentes; usar una lista grande (p. ej. HIBP) si el registro crece mucho.
        private static final Set<String> COMUNES = Set.of(
                "1234567890", "12345678910", "0123456789", "password123", "password1234", "contrasena1",
                "contrasena123", "qwerty1234", "qwertyuiop1", "abc1234567", "1q2w3e4r5t", "iloveyou12",
                "admin12345", "atlas12345", "cinesiterapia1", "fisioterapia1", "colombia123", "bogota12345");

        @Override
        public boolean isValid(String valor, ConstraintValidatorContext contexto) {
            if (valor == null) {
                return true; // @NotBlank se encarga
            }
            return valor.length() >= MIN
                    && valor.codePoints().anyMatch(Character::isLetter)
                    && valor.codePoints().anyMatch(Character::isDigit)
                    && !COMUNES.contains(valor.toLowerCase(Locale.ROOT));
        }
    }
}
