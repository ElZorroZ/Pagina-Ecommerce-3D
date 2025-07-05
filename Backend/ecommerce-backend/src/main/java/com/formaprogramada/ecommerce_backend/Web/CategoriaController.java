package com.formaprogramada.ecommerce_backend.Web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaCrearRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaUpdateRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Categoria.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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




    @PutMapping("/crearCategoria")
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


    @GetMapping("/leerCategoriaTodas")
    public Map<CategoriaEntity,String> leerCategoriaTodas() {
        try {
            Map<CategoriaEntity,String> lista= new HashMap<CategoriaEntity, String>();


            lista=categoriaService.LeerCategorias(lista);
            System.out.println("Se logro conseguir las categorias");
            for (Map.Entry<CategoriaEntity,String> lista2: lista.entrySet()) {
                System.out.println(lista2);
            }
            return lista;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @GetMapping("/leerCategoria/{id}")
    public Map<CategoriaEntity,String> leerCategoriaUna(@PathVariable int id) {
        try {

            Categoria categoria= new Categoria();
            categoria.setId(id);
            Map<CategoriaEntity,String> cate=categoriaService.LeerCategoria(categoria);
            System.out.println(cate);
            return cate;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }



    @PutMapping("/modificarCategoria/{id}")
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

    @PutMapping("/modificarCategoriaImagen/{id}")
    public ResponseEntity<?> modificarCategoriaImagen(
            @PathVariable int id,
            @RequestPart("file") MultipartFile file) {
        try {
            if(categoriaService.ModificarCategoriaImagen(file, id)) {
                return ResponseEntity.ok("Se hizo bien");
            }else{
                return ResponseEntity.ok("Se hizo mal");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/borrarCategoria/{id}")
    public ResponseEntity<Void> borrarCategoria(
            @PathVariable int id) {
        try {
            categoriaService.BorrarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/AgregarCategoriaDestacada/{id}")
    public ResponseEntity<?> AgregarCategoriaDestacada(
            @PathVariable int id) {
            if(categoriaService.AgregarCategoriaDestacada(id)) {
                return ResponseEntity.ok("Se hizo bien");
            }else{
                return ResponseEntity.ok("Se hizo mal");
            }
    }




}
