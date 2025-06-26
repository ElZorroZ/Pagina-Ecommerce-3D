package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.*;
import com.formaprogramada.ecommerce_backend.Mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categoria")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PutMapping("/crear_categoria")
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody CategoriaCrearRequest categoriaCrearRequest) {
        try {
            var categoria = CategoriaMapper.toDomain(categoriaCrearRequest);

            categoria = categoriaService.CrearCategoria(categoria);
            return ResponseEntity.ok("Se hizo bien");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
