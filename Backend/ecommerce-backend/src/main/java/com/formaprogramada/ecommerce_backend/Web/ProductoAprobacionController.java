package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador.ProductoArchivoColaboradorService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador.ProductoColaboradorService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoCompletoAprobacionDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoArchivoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar.ProductoAprobarMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@NoArgsConstructor
@AllArgsConstructor
@RequestMapping("/api/productosAprobacion")
public class ProductoAprobacionController {
    @Autowired
    private ProductoColaboradorService productoAprobadoColaboradorService;
    @Autowired
    private ProductoArchivoColaboradorService archivoService;


    @PostMapping(path = "/crearAprobacionProducto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoAprobacionResponse> crearAprobacionProducto(
            @RequestPart("producto") ProductoAprobacionRequest dto,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo) throws IOException {

        ProductoAprobacionResponse response = productoAprobadoColaboradorService.crearAprobacionProducto(dto, archivo);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/AprobarProducto")

    public ResponseEntity<?> aprobarProducto(
            @RequestParam("id") int id,
            @RequestParam("codigoInicial") String codigoInicial,
            @RequestParam("versionStr") String versionStr,
            @RequestParam("seguimiento") String seguimiento)
            {

            productoAprobadoColaboradorService.aprobarProducto(id,codigoInicial,versionStr,seguimiento);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/BorrarProducto")
    public ResponseEntity<?> borrarProductoColaborador(
            @RequestParam("id") int id)
    {

        productoAprobadoColaboradorService.borrarProducto(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/VerProductos")

    public ResponseEntity<?> verProductos()
    {
        try {
            List<ProductoCompletoAprobacionDTO> productosList = productoAprobadoColaboradorService.verProductosaAprobar();
            if (productosList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay productos disponibles");
            }
            return ResponseEntity.ok(productosList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los productos: " + e.getMessage());
        }
    }


    @GetMapping("/VerProductos_de/{id}")
    public ResponseEntity<?> verProductosDeUsuario(@PathVariable int id) {
        try {
            List<ProductoCompletoAprobacionDTO> productosList = productoAprobadoColaboradorService.verProductosaAprobarDeX(id);
            if (productosList.isEmpty()) {
                System.out.println("No hay productos para el usuario id = " + id);
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(productosList);
        } catch (Exception e) {
            System.err.println("Error en verProductosDeUsuario:");
            e.printStackTrace();  // ¡Este es clave para ver el stacktrace!
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los productos: " + e.getMessage());
        }
    }

    @GetMapping("/VerProductoCompleto/{id}")
    public ResponseEntity<?> verProductoCompleto(@PathVariable int id) {
        try {
            ProductoCompletoAprobacionDTO producto = productoAprobadoColaboradorService.obtenerProductoCompleto(id);
            if (producto == null) {
                System.out.println("No se encontró el producto con id = " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Producto no encontrado");
            }
            return ResponseEntity.ok(producto);
        } catch (Exception e) {
            System.err.println("Error en verProductoCompleto:");
            e.printStackTrace(); // Mantener para depuración
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el producto: " + e.getMessage());
        }
    }

    @PutMapping(value = "/ActualizarProductoAprobar/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Integer id,
            @RequestPart("producto") ProductoCompletoAprobacionDTO productoCompletoDTO,
            @RequestPart(value = "archivosNuevos", required = false) List<MultipartFile> archivosNuevos,
            @RequestPart(value = "archivoComprimido", required = false) MultipartFile archivoComprimido,
            @RequestParam(value = "eliminarArchivoComprimido", required = false) String eliminarArchivoComprimido
    ) {
        try {
            ProductoAprobacionEntity actualizado = productoAprobadoColaboradorService
                    .actualizarProductoCompleto(id, productoCompletoDTO, archivosNuevos, archivoComprimido, eliminarArchivoComprimido);

            ProductoAprobacionResponseDTO dto = ProductoAprobarMapper.toDTO(actualizado);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    //ENDPOINTS DE ARCHIVOS DE PRODUCTO

    @PostMapping(value = "/{productoId}/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoArchivoResponse> agregarArchivo(
            @PathVariable Integer productoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("orden") Integer orden) {
        try {
            ProductoArchivoAprobacionEntity creado = archivoService.agregarArchivo(productoId, file, orden);
            return ResponseEntity.ok(new ProductoArchivoResponse(creado));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ProductoArchivoResponse("Error: " + e.getMessage()));
        }
    }



}
