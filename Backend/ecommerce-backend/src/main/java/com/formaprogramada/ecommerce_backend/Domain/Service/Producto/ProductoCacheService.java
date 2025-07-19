package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Domain.Repository.Categoria.CategoriaRepository;
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
public class ProductoCacheService {

    private final ProductoService productoService;
    @Autowired
    CacheManager cacheManager;
    @Autowired
    JpaCategoriaRepository categoriaRepository;
    public ProductoCacheService(@Lazy ProductoService productoService) {
        this.productoService = productoService;
    }
    @Transactional
    public void precargarCacheProductos() {
        List<Integer> ids = productoService.obtenerTodosLosIds();
        Cache cache = cacheManager.getCache("productoCompleto");
        for (Integer id : ids) {
            ProductoCompletoDTO dto = productoService.obtenerProductoCompletoSinCache(id);
            cache.put(id, dto);
        }
    }
    @Transactional
    public void precargarCacheProductosTodos(Pageable pageable) {
        productoService.obtenerTodosLosProductosConColoresYArchivo(pageable);
    }
    @Transactional
    public void precargarCachePorCategoria(Pageable pageable) {
        List<Integer> categoriasIds = categoriaRepository.findAllIds();

        for (Integer id : categoriasIds) {
            productoService.listarProductosPorCategoria(id, pageable);
        }
    }


    @Transactional
    public void precargarCacheProductosDestacados(Pageable pageable) {
        productoService.obtenerTodosConArchivoPrincipalYColores(pageable);
    }

    // Refresca el cache de un producto específico (llama a obtenerProductoCompleto)
    public void refrescarCacheProducto(Integer id) {
        productoService.obtenerProductoCompleto(id);
    }
    // Borra y vuelve a precargar todos los productos (opcional)
    public void refrescarTodosLosProductos() {
        List<Integer> ids = productoService.obtenerTodosLosIds();

        for (Integer id : ids) {
            productoService.obtenerProductoCompleto(id); // cachea de nuevo
        }
    }
    @Transactional
    public void precargarOtrosCaches() {
        productoService.listarProductos(); // cachea "productos"
        productoService.obtenerTodosConArchivoPrincipalYColores(PageRequest.of(0, 10)); // cachea "productosDestacados" o paginado
    }
}
