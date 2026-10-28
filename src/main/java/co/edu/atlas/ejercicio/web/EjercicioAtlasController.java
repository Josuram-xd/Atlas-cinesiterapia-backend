package co.edu.atlas.ejercicio.web;

import co.edu.atlas.ejercicio.dto.EjercicioResponse;
import co.edu.atlas.ejercicio.dto.EjercicioResumenResponse;
import co.edu.atlas.ejercicio.service.EjercicioAtlasService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ejercicios")
@Validated
public class EjercicioAtlasController {

    private final EjercicioAtlasService service;

    public EjercicioAtlasController(EjercicioAtlasService service) {
        this.service = service;
    }

    @GetMapping
    public List<EjercicioResumenResponse> listar(
            @RequestParam(required = false) Long region, @RequestParam(required = false) Long categoria) {
        return service.listar(region, categoria);
    }

    @GetMapping("/buscar")
    public List<EjercicioResumenResponse> buscar(
            @RequestParam @NotBlank(message = "Escribe un texto para buscar") @Size(max = 100, message = "Maximo 100 caracteres") String q) {
        return service.buscar(q);
    }

    @GetMapping("/{id}")
    public EjercicioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }
}
