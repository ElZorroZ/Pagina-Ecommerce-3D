package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Domain.Model.Producto.Producto;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDestacadoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoArchivoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoColorRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoDestacadoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ArchivoMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoDTOMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoDestacadoServiceImpl implements ProductoDestacadoService {
    @Autowired
    private final JpaProductoRepository productoRepository;
    @Autowired
    private final JpaProductoDestacadoRepository destacadoRepository;
    @Autowired
    private final JpaProductoColorRepository productoColorRepository;
    @Autowired
    private final JpaProductoArchivoRepository productoArchivoRepository;
    @Autowired
    private CacheManager cacheManager;
    private static final Logger logger = LoggerFactory.getLogger(ProductoDestacadoServiceImpl.class);
    @Override
    public void toggleProductoDestacado(Integer productoId) {
        logger.info("🔄 toggleProductoDestacado - Iniciando con productoId: {}", productoId);

        Optional<ProductoDestacadoEntity> existente = destacadoRepository.findByProductoId(productoId);
        boolean esDestacadoAhora;

        if (existente.isPresent()) {
            destacadoRepository.deleteById(existente.get().getId());
            logger.info("🗑️ Producto removido de destacados en la base de datos");
            esDestacadoAhora = false;
        } else {
            ProductoEntity producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            ProductoDestacadoEntity nuevo = new ProductoDestacadoEntity();
            nuevo.setProducto(producto);
            destacadoRepository.save(nuevo);
            logger.info("✅ Producto agregado como destacado en la base de datos");
            esDestacadoAhora = true;
        }

        // 🔁 Actualizar cache productosDestacados
        try {
            Cache cache = cacheManager.getCache("productosDestacados");
            if (cache != null) {
                Object destacadosObj = cache.get("destacados", Object.class);
                List<ProductoConArchivoPrincipalYColoresDTO> destacadosActuales;

                if (destacadosObj instanceof List<?> lista) {
                    destacadosActuales = new ArrayList<>((List<ProductoConArchivoPrincipalYColoresDTO>) lista); // ✅ FIX: crear copia mutable
                } else {
                    destacadosActuales = new ArrayList<>();
                }

                if (!esDestacadoAhora) {
                    destacadosActuales.removeIf(d -> d.getProducto().getId().equals(productoId));
                    logger.info("❎ Producto removido del cache de productosDestacados");
                } else {
                    ProductoEntity producto = productoRepository.findById(productoId)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                    ProductoResponseDTO responseDTO = ProductoMapper.toDTO(producto);
                    ProductoDTO dto = ProductoDTOMapper.fromResponseDTO(responseDTO);

                    List<String> colores = productoColorRepository.findByProductoId(productoId).stream()
                            .map(ProductoColorEntity::getColor)
                            .collect(Collectors.toList());

                    ArchivoDTO archivo = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(productoId).stream()
                            .findFirst().map(ArchivoMapper::toArchivoDTO).orElse(null);

                    ProductoConArchivoPrincipalYColoresDTO nuevoDestacado = new ProductoConArchivoPrincipalYColoresDTO();
                    nuevoDestacado.setProducto(dto);
                    nuevoDestacado.setColores(colores);
                    nuevoDestacado.setArchivoPrincipal(archivo);

                    destacadosActuales.add(nuevoDestacado);
                    logger.info("✅ Producto agregado al cache de productosDestacados");
                }

                cache.put("destacados", destacadosActuales); // podés usar List.copyOf(destacadosActuales) si querés que vuelva a ser inmutable
            }
        } catch (Exception e) {
            logger.error("❌ Error actualizando cache 'productosDestacados'", e);
        }

        // 🔁 Actualizar cache productos
        try {
            Cache cacheProductos = cacheManager.getCache("productos");
            if (cacheProductos != null) {
                Object productosObj = cacheProductos.get("productos", Object.class);
                if (productosObj instanceof List<?> lista) {
                    boolean actualizado = false;
                    for (Object obj : lista) {
                        if (obj instanceof ProductoResponseConDestacado p && p.getId().equals(productoId)) {
                            p.setDestacado(esDestacadoAhora);
                            actualizado = true;
                            break;
                        }
                    }
                    if (actualizado) {
                        cacheProductos.put("productos", lista);
                        logger.info("✅ Cache 'productos' actualizado con nuevo estado de destacado");
                    } else {
                        logger.warn("⚠️ Producto con ID {} no encontrado en cache 'productos'", productoId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("❌ Error actualizando cache 'productos'", e);
        }

        // 🔁 Evict otras caches por ID
        try {
            logger.info("🧹 Limpiando cache por ID {}", productoId);
            Optional.ofNullable(cacheManager.getCache("producto")).ifPresent(c -> c.evict(productoId));
            Optional.ofNullable(cacheManager.getCache("productosPorCategoria")).ifPresent(c -> c.evict(productoId));
            Optional.ofNullable(cacheManager.getCache("productoCompleto")).ifPresent(c -> c.evict(productoId));
            logger.info("✅ Caches 'producto', 'productosPorCategoria', 'productoCompleto' evicted");
        } catch (Exception e) {
            logger.error("❌ Error evicting caches por ID", e);
        }

        logger.info("🏁 toggleProductoDestacado - Finalizado para productoId: {}", productoId);
    }


}
