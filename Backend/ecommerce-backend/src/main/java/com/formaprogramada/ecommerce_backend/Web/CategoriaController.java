package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaCrearRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaUpdateRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Categoria.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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


    @GetMapping("/leer_categoria_todas")
    public List<CategoriaEntity> leerCategoriaTodas() {
        try {
            List<CategoriaEntity> lista = new ArrayList<>();

            lista=categoriaService.LeerCategorias(lista);
            System.out.println("Se logro conseguir las categorias");
            for (CategoriaEntity categoria : lista) {
                System.out.println(categoria);
            }
            return lista;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @GetMapping("/leer_categoria_/{id}")
    public Optional<CategoriaEntity> leerCategoriaUna(@PathVariable int id) {
        try {

            Categoria categoria= new Categoria();
            categoria.setId(id);
            Optional<CategoriaEntity> cate=categoriaService.LeerCategoria(categoria);
            System.out.println(cate);
            return cate;
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }



    @PutMapping("/modificar_categoria/{id}")
    public ResponseEntity<?> modificarCategoria(
            @PathVariable int id,
            @Valid @RequestBody CategoriaUpdateRequest categoriaUpdateRequest) {
        try {
            Categoria categoria=CategoriaMapper.toDomain2(categoriaUpdateRequest);
            categoria = categoriaService.ModificarCategoria(categoria, id);
            return ResponseEntity.ok("Se hizo bien");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/borrar_categoria/{id}")
    public ResponseEntity<Void> borrarCategoria(
            @PathVariable int id) {
        try {
            categoriaService.BorrarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }


}
