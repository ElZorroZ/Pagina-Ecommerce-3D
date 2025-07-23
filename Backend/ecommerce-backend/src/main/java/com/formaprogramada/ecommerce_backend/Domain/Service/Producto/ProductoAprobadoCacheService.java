package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
