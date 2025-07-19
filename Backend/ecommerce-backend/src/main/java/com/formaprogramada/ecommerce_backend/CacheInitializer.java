package com.formaprogramada.ecommerce_backend;

import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoCacheService;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class CacheInitializer {

    private final ProductoCacheService productoCacheService;

    public CacheInitializer(ProductoCacheService productoCacheService) {
        this.productoCacheService = productoCacheService;
    }

    @PostConstruct
    public void cargarCacheAlIniciar() {
        productoCacheService.precargarCacheProductos();
        productoCacheService.precargarOtrosCaches();
        productoCacheService.precargarCacheProductosTodos(PageRequest.of(0, 20));
        productoCacheService.precargarCacheProductosDestacados(PageRequest.of(0, 10));
        productoCacheService.precargarCachePorCategoria(PageRequest.of(0, 10));  // Precarga cache por cada categoría
    }
}
