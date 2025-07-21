package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDetalleAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class ProductoAprobadoServiceImpl implements ProductoAprobadoService{
    @Autowired
    private JpaProductoRepository productoRepository;
    @Autowired
    private JpaProductoAprobacionRepository productoAprobacionRepository;
    @Autowired
    private JpaProductoColorRepository productoColorRepository;
    @Autowired
    private JpaProductoColorAprobacionRepository productoColorAprobacionRepository;
    @Autowired
    private JpaProductoArchivoRepository productoArchivoRepository;
    @Autowired
    private JpaProductoDetalleRepository productoDetalleRepository;
    @Autowired
    private JpaProductoDetalleAprobacionRepository productoDetalleAprobacionRepository;
    @Autowired
    private ProductoCacheService productoCacheService;
    @Autowired
    private JpaProductoDestacadoRepository productoDestacadoRepository;
    @Autowired
    private JpaCategoriaRepository categoriaRepository;
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImpl.class);
    @Autowired
    private CacheManager cacheManager;

    @Caching(evict = {
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productosDestacados", allEntries = true)
    })

    @Override
    @Transactional
    public ProductoResponse crearAprobacionProducto(ProductoAprobacionRequest dto, MultipartFile archivoStl) throws IOException {
        // Construir código concatenado de forma segura
        String codigoInicial = dto.getCodigoInicial() != null ? dto.getCodigoInicial() : "";
        String versionStr = dto.getVersion() != null ? dto.getVersion() : "";
        String seguimiento = dto.getSeguimiento() != null ? dto.getSeguimiento() : "";
        String codigo = codigoInicial + versionStr + seguimiento;


        // Crear y guardar producto base
        ProductoAprobacionEntity producto = new ProductoAprobacionEntity();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setCodigo(codigo);
        producto.setUsuarioId(dto.getCreadorId());

        // archivoStl es MultipartFile
        if (archivoStl != null && !archivoStl.isEmpty()) {
            producto.setArchivo(archivoStl.getBytes()); // No uses decode acá
        }

        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoriaId(categoria);

        ProductoAprobacionEntity productoGuardado = productoAprobacionRepository.save(producto);

        // Construir string dimension (alto x ancho x profundidad)
        String dimension = dto.getDimensionAlto() + "x" + dto.getDimensionAncho() + "x" + dto.getDimensionProfundidad();

        // Guardar detalle producto
        ProductoDetalleAprobacionEntity detalle = ProductoDetalleAprobacionEntity.builder()
                .productoId(productoGuardado.getId())
                .dimension(dimension)
                .material(dto.getMaterial())
                .tecnica(dto.getTecnica())
                .peso(dto.getPeso() != null ? dto.getPeso() : "")
                .build();

        productoDetalleAprobacionRepository.save(detalle);

        // Guardar colores
        productoColorAprobacionRepository.deleteByProducto_Id(productoGuardado.getId());
        List<String> colores = dto.getColores();
        if (colores != null && !colores.isEmpty()) {
            List<ProductoColorAprobacionEntity> coloresEntities = colores.stream()
                    .map(color -> new ProductoColorAprobacionEntity(0, productoGuardado, color))
                    .toList();
            productoColorAprobacionRepository.saveAll(coloresEntities);
        }

        // Obtener colores guardados para la respuesta
        List<String> coloresGuardados = productoColorAprobacionRepository.findByProductoId(productoGuardado.getId())
                .stream()
                .map(ProductoColorAprobacionEntity::getColor)
                .toList();


/*
        ProductoCompletoAprobacionDTO completo = null;
        for (int i = 0; i < 3; i++) {
            completo = obtenerProductoCompletoSinCache(productoGuardado.getId());
            if (completo != null && completo.getProducto() != null) break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Buena práctica: volver a marcar el hilo como interrumpido
                throw new RuntimeException("Interrumpido al esperar para volver a obtener el producto completo", e);
            }
        }

        if (completo != null && completo.getProducto() != null) {
            cacheManager.getCache("productoCompleto").put(productoGuardado.getId(), completo);
        }
        productoCacheService.refrescarCacheProducto(productoGuardado.getId());
*/
        return new ProductoResponse(productoGuardado, detalle, coloresGuardados);
    }
}
