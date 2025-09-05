package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Elastic.ProductoBusquedaDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Elastic.ProductoSimpleDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Elastic.ProductoDocument;
import com.formaprogramada.ecommerce_backend.Infrastructure.Elastic.ProductoSearchService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Elastic.ProductoSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos/busqueda")
@CrossOrigin(origins = "*")
@Profile("prod")
public class ProductoBusquedaController {

    @Autowired
    private ProductoSearchService productoSearchService;

    @Autowired
    private ProductoSyncService productoSyncService;

    @Autowired
    private ProductoService productoService;

    @GetMapping("/simple")
    public ResponseEntity<List<ProductoSimpleDTO>> busquedaSimple(
            @RequestParam String q,
            @RequestParam(defaultValue = "relevancia") String ordenarPor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductoDocument> resultados = productoSearchService.busquedaSimple(q, ordenarPor, pageable);

        List<ProductoSimpleDTO> productos = resultados.getContent().stream()
                .map(doc -> {
                    ProductoCompletoDTO completo = productoService.obtenerProductoCompleto(doc.getId());
                    ProductoDTO p = completo.getProducto();

                    // Obtener link del primer archivo, si existe
                    String link = (completo.getArchivos() != null && !completo.getArchivos().isEmpty())
                            ? completo.getArchivos().get(0).getLinkArchivo()
                            : null;

                    return new ProductoSimpleDTO(p.getId(), p.getNombre(), p.getPrecio(), link);
                })
                .collect(Collectors.toList());


        return ResponseEntity.ok(productos);
    }






    @PostMapping("/avanzada")
    public ResponseEntity<Page<ProductoDocument>> busquedaAvanzada(
            @RequestBody ProductoBusquedaDTO filtros,
            @RequestParam(defaultValue = "relevancia") String ordenarPor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        System.out.println("🔍 Llamada a busquedaAvanzada con filtros=" + filtros + ", ordenarPor=" + ordenarPor + ", page=" + page + ", size=" + size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductoDocument> resultados = productoSearchService.busquedaAvanzada(filtros, ordenarPor, pageable);

        System.out.println("Resultados encontrados: " + resultados.getTotalElements());
        return ResponseEntity.ok(resultados);
    }

    // Controller
    @GetMapping("/sugerencias")
    public ResponseEntity<List<Map<String, Object>>> obtenerSugerencias(@RequestParam String q) {
        System.out.println("🔍 Llamada a obtenerSugerencias con q=" + q);

        // Obtenemos productos que coinciden con el texto
        List<ProductoDocument> productos = productoSearchService.obtenerSugerenciasConIds(q, 10);

        // Creamos lista de sugerencias con id + nombre
        List<Map<String, Object>> sugerencias = productos.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("nombre", p.getNombre());
                    return map;
                })
                .collect(Collectors.toList());

        System.out.println("Sugerencias encontradas: " + sugerencias);
        return ResponseEntity.ok(sugerencias);
    }




}
