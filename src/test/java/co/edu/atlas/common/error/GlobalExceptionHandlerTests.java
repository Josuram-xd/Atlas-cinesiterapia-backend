package co.edu.atlas.common.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
@Import(GlobalExceptionHandlerTests.ThrowingController.class)
class GlobalExceptionHandlerTests {

    @Autowired
    MockMvc mvc;

    @Test
    void apiExceptionKeepsStatusAndCode() throws Exception {
        mvc.perform(get("/test-errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"))
                .andExpect(jsonPath("$.message").value("ya existe"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/test-errors/conflict"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void bodyValidationListsFieldErrors() throws Exception {
        mvc.perform(post("/test-errors/body").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void malformedJsonIsBadRequest() throws Exception {
        mvc.perform(post("/test-errors/body").contentType(MediaType.APPLICATION_JSON).content("{nope"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void missingParamIsBadRequest() throws Exception {
        mvc.perform(get("/test-errors/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void accessDeniedIsForbidden() throws Exception {
        mvc.perform(get("/test-errors/denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void unexpectedErrorHidesDetails() throws Exception {
        mvc.perform(get("/test-errors/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Error interno del servidor"));
    }

    @Test
    void unknownPathIsNotFound() throws Exception {
        mvc.perform(get("/no-such-path"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void wrongMethodIsMethodNotAllowed() throws Exception {
        mvc.perform(post("/test-errors/conflict"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void openApiDocsAreServed() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    record Body(@NotBlank String name) {}

    @TestConfiguration
    @RestController
    static class ThrowingController {
        @GetMapping("/test-errors/conflict")
        String conflict() {
            throw ApiException.conflict("DUPLICATE", "ya existe");
        }

        @PostMapping("/test-errors/body")
        String body(@Valid @RequestBody Body body) {
            return body.name();
        }

        @GetMapping("/test-errors/param")
        String param(@RequestParam String q) {
            return q;
        }

        @GetMapping("/test-errors/denied")
        String denied() {
            throw new AccessDeniedException("no");
        }

        @GetMapping("/test-errors/boom")
        String boom() {
            throw new IllegalStateException("secreto interno");
        }
    }
}
