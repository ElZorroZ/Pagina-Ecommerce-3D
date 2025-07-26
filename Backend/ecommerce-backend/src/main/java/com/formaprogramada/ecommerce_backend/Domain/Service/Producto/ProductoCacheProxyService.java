package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductoCacheProxyService {
    @Autowired
    private ProductoService productoService;

    // Llamar para precargar la lista completa en cache
    public void precargarProductosTodos() {
        productoService.obtenerTodosLosProductosSinPaginado();
    }

    public void precargarPorCategoria(Integer categoriaId, Pageable pageable) {
        productoService.listarProductosPorCategoria(categoriaId, pageable);
    }

    public void precargarProductosDestacados() {
        productoService.obtenerProductosDestacados();
    }

    public void precargarProductosResumen() {
        productoService.listarProductos(); // cachea "productos"
    }

}

