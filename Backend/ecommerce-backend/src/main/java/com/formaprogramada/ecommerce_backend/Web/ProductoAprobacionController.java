package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoCompletoAprobacionDTO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@NoArgsConstructor
@AllArgsConstructor
@RequestMapping("/api/productosAprobacion")
public class ProductoAprobacionController {
    @Autowired
    private ProductoAprobadoService productoAprobadoService;

    @Autowired
    private ProductoArchivoService archivoService;

    @Autowired
    private ImgBBUploaderService imgBBUploaderService;

    @Autowired
    private ProductoDestacadoService productoDestacadoService;


    @PostMapping(path = "/crearAprobacionProducto",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoAprobacionResponse> crearAprobacionProducto(
            @RequestPart("producto") ProductoAprobacionRequest dto,
            @RequestPart(value = "producto", required = false) MultipartFile archivo) throws IOException {

        ProductoAprobacionResponse response = productoAprobadoService.crearAprobacionProducto(dto, archivo);
        System.out.println(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/AprobarProducto")
    public ResponseEntity<?> aprobarProducto(
            @RequestParam("id") int id,
            @RequestParam("codigoInicial") String codigoInicial,
            @RequestParam("versionStr") String versionStr,
            @RequestParam("seguimiento") String seguimiento)
            {

            productoAprobadoService.aprobarProducto(id,codigoInicial,versionStr,seguimiento);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/BorrarProducto")
    public ResponseEntity<?> borrarProducto(
            @RequestParam("id") int id)
    {

        productoAprobadoService.borrarProducto(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/VerProductos")
    public ResponseEntity<?> verProductos()
    {
        try {
            List<ProductoCompletoAprobacionDTO> productosList = productoAprobadoService.verProductosaAprobar();
            if (productosList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay productos disponibles");
            }
            System.out.println(productosList);
            return ResponseEntity.ok(productosList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los productos: " + e.getMessage());
        }
    }

    @GetMapping("/VerProductos_de/{id}")
    public ResponseEntity<?> verProductosDeX(@PathVariable int id)
    {
        try {
            List<ProductoCompletoAprobacionDTO> productosList = productoAprobadoService.verProductosaAprobarDeX(id);
            if (productosList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay productos disponibles");
            }
            System.out.println(productosList);
            return ResponseEntity.ok(productosList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los productos: " + e.getMessage());
        }
    }

    @GetMapping("/VerProductoCompleto/{id}")
    public ResponseEntity<?> verProductoAlCompleto(@PathVariable int id)
    {
        try {
            List<ProductoCompletoAprobacionDTO> productosList = productoAprobadoService.verProductoCompleto(id);
            if (productosList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay productos disponibles");
            }
            System.out.println(productosList);
            return ResponseEntity.ok(productosList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los productos: " + e.getMessage());
        }
    }







}
