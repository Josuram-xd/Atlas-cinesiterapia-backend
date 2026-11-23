package co.edu.atlas.catalogo.web;

import co.edu.atlas.catalogo.Vista;
import co.edu.atlas.catalogo.dto.CategoriaConConteoResponse;
import co.edu.atlas.catalogo.dto.CategoriaResponse;
import co.edu.atlas.catalogo.dto.RegionResponse;
import co.edu.atlas.catalogo.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogoController {

    private final CatalogoService service;

    public CatalogoController(CatalogoService service) {
        this.service = service;
    }

    @GetMapping("/regiones")
    public List<RegionResponse> regiones(@RequestParam(required = false) Vista vista) {
        return service.regiones(vista);
    }

    @GetMapping("/regiones/{id}/categorias")
    public List<CategoriaConConteoResponse> categoriasDeRegion(@PathVariable Long id) {
        return service.categoriasDeRegion(id);
    }

    @GetMapping("/categorias")
    public List<CategoriaResponse> categorias() {
        return service.categorias();
    }
}
