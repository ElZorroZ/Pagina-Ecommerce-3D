package com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ProductoAprobadoCacheService {

    private final ProductoAprobadoService productoAprobadoService;
    @Autowired
    CacheManager cacheManager;
    @Autowired
    JpaCategoriaRepository categoriaRepository;
    public ProductoAprobadoCacheService(@Lazy ProductoAprobadoService productoAprobadoService) {
        this.productoAprobadoService = productoAprobadoService;
    }
    public void refrescarCacheProducto(Integer id) {
        productoAprobadoService.obtenerProductoCompleto(id);
    }
    public void refrescarTodosLosProductos() {
        List<Integer> ids = productoAprobadoService.obtenerTodosLosIds();

        for (Integer id : ids) {
            productoAprobadoService.obtenerProductoCompleto(id); // cachea de nuevo
        }
    }

    public void precargarUltimoProducto() {
        // Limpia la entrada actual para que se recargue
        cacheManager.getCache("ultimoProducto").evict("ultimo");

        // Fuerza la recarga del caché llamando al método cacheado
        productoAprobadoService.obtenerUltimoProducto();
    }
}
