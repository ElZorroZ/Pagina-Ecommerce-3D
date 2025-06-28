package com.formaprogramada.ecommerce_backend.Web;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categoria")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;
    @Autowired
    private ImgBBUploaderService imgBBUploaderService;

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

    @GetMapping("/leerCategoria/{id}")
    public CategoriaEntity leerCategoriaUna(@PathVariable int id) {
        try {

            Categoria categoria= new Categoria();
            categoria.setId(id);
            CategoriaEntity cate=categoriaService.LeerCategoria(categoria);
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


}
