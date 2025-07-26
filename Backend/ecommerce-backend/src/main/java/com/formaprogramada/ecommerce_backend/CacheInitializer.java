package com.formaprogramada.ecommerce_backend;

import com.formaprogramada.ecommerce_backend.Domain.Service.Categoria.CategoriaCacheProxy;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoCacheProxyService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoCacheService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CacheInitializer {


    private final ProductoCacheService productoCacheService;
    @Autowired
    private ProductoCacheProxyService productoCacheProxyService;
    @Autowired
    private JpaCategoriaRepository jpaCategoriaRepository;
    @Autowired
    private CategoriaCacheProxy categoriaCacheProxyService;

    public CacheInitializer(ProductoCacheService productoCacheService) {
        this.productoCacheService = productoCacheService;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional// Este método ahora sí correrá dentro de una transacción
    public void cargarCacheAlIniciar() {
        //Categorias
        // Precargar lista de categorías
        categoriaCacheProxyService.precargarCategoriasLista();

        // Precargar cada categoría individual
        List<Integer> ids = jpaCategoriaRepository.findAllIds();
        for (Integer id : ids) {
            categoriaCacheProxyService.precargarCategoriaIndividual(id);
        }
        categoriaCacheProxyService.precargarCategoriasCombo();

        //Productos
        productoCacheProxyService.precargarProductosTodos();
        productoCacheService.precargarCacheProductos();
        productoCacheService.precargarOtrosCaches();
        productoCacheService.precargarCacheProductosTodos(PageRequest.of(0, 20));
        productoCacheProxyService.precargarProductosDestacados();
        productoCacheProxyService.precargarProductosResumen();
        // Precargar cache de productosPorCategoria para todas las categorías con bucle
        List<Integer> categoriasIds = jpaCategoriaRepository.findAllIds();
        for (Integer categoriaId : categoriasIds) {
            productoCacheProxyService.precargarPorCategoria(categoriaId, PageRequest.of(0, 20));
        }
    }

}
