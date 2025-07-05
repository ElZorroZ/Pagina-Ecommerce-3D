package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.MaxDestacadosException;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoArchivoService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoDestacadoService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoArchivoService archivoService;

    @Autowired
    private ImgBBUploaderService imgBBUploaderService;

    @Autowired
    private ProductoDestacadoService productoDestacadoService;

    //ENDPOINTS DE PRODUCTO

    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@RequestBody ProductoRequestConColores dto) {
        System.out.println("Colores del request: " + dto.getColores());
        ProductoEntity creado = productoService.crearProducto(dto, dto.getColores());
        return ResponseEntity.ok(new ProductoResponse(creado));
    }


    @GetMapping
    public ResponseEntity<?> obtenerProductos() {
        try {
            List<ProductoResponseConDestacado> productos = productoService.listarProductos();
            return ResponseEntity.ok(productos); // 200 OK con la lista
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los productos: " + e.getMessage());
        }
    }

    @GetMapping("/completo")
    public ResponseEntity<?> obtenerTodosLosProductosCompletos() {
        try {
            List<ProductoConArchivoPrincipalYColoresDTO> productosCompletos = productoService.obtenerTodosConArchivoPrincipalYColores();

            if (productosCompletos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay productos disponibles");
            }

            return ResponseEntity.ok(productosCompletos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los productos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProductoPorId(@PathVariable Integer id) {
        try {
            ProductoCompletoDTO productoCompleto = productoService.obtenerProductoCompleto(id);
            if (productoCompleto == null || productoCompleto.getProducto() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
            }
            return ResponseEntity.ok(productoCompleto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el producto: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Integer id,
            @RequestPart("producto") ProductoCompletoDTO productoCompletoDTO,
            @RequestPart(value = "archivosNuevos", required = false) List<MultipartFile> archivosNuevos) {

        try {
            ProductoEntity actualizado = productoService.actualizarProductoCompleto(id, productoCompletoDTO, archivosNuevos);
            ProductoResponseDTO dto = ProductoMapper.toDTO(actualizado);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    //ENDPOINTS DE DESTACADOS DE PRODUCTO


    @PostMapping("/{id}/destacado")
    public ResponseEntity<?> toggleDestacado(@PathVariable Integer id) {
        try {
            productoDestacadoService.toggleProductoDestacado(id);
            return ResponseEntity.ok().build();
        } catch (MaxDestacadosException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar destacado: " + e.getMessage());
        }
    }

    //ENDPOINTS DE ARCHIVOS DE PRODUCTO

    @PostMapping(value = "/{productoId}/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoArchivoResponse> agregarArchivo(
            @PathVariable Integer productoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("orden") Integer orden) {
        try {
            ProductoArchivoEntity creado = archivoService.agregarArchivoConImagen(productoId, file, orden);
            return ResponseEntity.ok(new ProductoArchivoResponse(creado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/{productoId}/archivos")
    public ResponseEntity<List<ProductoArchivoResponse>> obtenerArchivos(@PathVariable Integer productoId) {
        List<ProductoArchivoEntity> entidades = archivoService.obtenerArchivosPorProductoId(productoId);
        List<ProductoArchivoResponse> dtos = entidades.stream()
                .map(ProductoArchivoResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }



}
