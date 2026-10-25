package co.edu.atlas.common.error;

import co.edu.atlas.common.error.ApiError.FieldViolation;
import java.util.List;
import org.springframework.http.HttpStatus;

/** Error de negocio con estado HTTP y codigo estable para el cliente. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<FieldViolation> violations;

    public ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, List.of());
    }

    public ApiException(HttpStatus status, String code, String message, List<FieldViolation> violations) {
        super(message);
        this.status = status;
        this.code = code;
        this.violations = List.copyOf(violations);
    }

    /** Detalle por campo que se devuelve en {@code fieldErrors}. */
    public List<FieldViolation> getViolations() {
        return violations;
    }

    public static ApiException unprocessable(String code, String message, List<FieldViolation> violations) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, code, message, violations);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public static ApiException badRequest(String code, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static ApiException unauthorized(String code, String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, code, message);
    }

    public static ApiException forbidden(String code, String message) {
        return new ApiException(HttpStatus.FORBIDDEN, code, message);
    }

    public static ApiException notFound(String code, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, code, message);
    }

    public static ApiException conflict(String code, String message) {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }

    public static ApiException tooManyRequests(String code, String message) {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, code, message);
    }
}
