package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CategoriaCreacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Mapper.CategoriaMapper;
import com.formaprogramada.ecommerce_backend.Mapper.UsuarioMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categoria")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody CategoriaCreacionRequest request) {
        Categoria categoria = CategoriaMapper.toDomainCategoria1(request);
        categoriaService.registrarCategoria(categoria);
        return ResponseEntity.ok("Usuario registrado correctamente.");
    }
}
