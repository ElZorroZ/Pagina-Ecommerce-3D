package com.formaprogramada.ecommerce_backend.Web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Service.Categoria.CategoriaService;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.MaxDestacadosException;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Categoria.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/categoria")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService categoriaService;
    @Autowired
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody CategoriaCrearRequest categoriaCrearRequest) {
        try {
            var categoria = CategoriaMapper.toDomain(categoriaCrearRequest);

            categoria = categoriaService.CrearCategoria(categoria);
            return ResponseEntity.ok("Se hizo bien");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/crearCategoriaConImagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearCategoriaConImagen(
            @RequestPart("categoria") CategoriaCrearRequest categoriaCrearRequest,
            @RequestPart("file") MultipartFile file) {
        try {
            var categoria = CategoriaMapper.toDomain(categoriaCrearRequest);
            categoria = categoriaService.CrearCategoriaConImagen(categoria, file);
            return ResponseEntity.ok("Se hizo bien");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> leerCategoriaTodas() {
        try {
            List<CategoriaDTO> lista = categoriaService.LeerCategorias();
            System.out.println("Se logró conseguir las categorías");
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            // Mejor manejar la excepción y devolver un error HTTP adecuado
            System.err.println("Error al obtener categorías: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combo")
    public ResponseEntity<List<CategoriaComboDTO>> leerCategoriasCombo() {
        try {
            List<CategoriaComboDTO> lista = categoriaService.LeerCategoriasCombo();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<CategoriaDTOconImagen> leerCategoria(@PathVariable int id) {
        try {
            CategoriaDTOconImagen dto = categoriaService.LeerCategoria(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificarCategoriaCompleta(
            @PathVariable int id,
            @RequestPart("categoria") @Valid CategoriaUpdateRequest categoriaUpdateRequest,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            // Actualizar datos categoría
            Categoria categoria = CategoriaMapper.toDomain2(categoriaUpdateRequest);
            categoriaService.ModificarCategoria(categoria, id);

            // Si vino archivo, actualizar imagen
            if (file != null && !file.isEmpty()) {
                categoriaService.ModificarCategoriaImagen(file, id);
            }

            return ResponseEntity.ok("Categoría actualizada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> borrarCategoria(
            @PathVariable int id) {
        try {
            categoriaService.BorrarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/toggleCategoriaDestacada/{id}")
    public ResponseEntity<?> toggleCategoriaDestacada(@PathVariable int id) {
        try {
            categoriaService.toggleCategoriaDestacada(id);
            return ResponseEntity.ok("Destacado actualizado correctamente");
        } catch (MaxDestacadosException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }





}
