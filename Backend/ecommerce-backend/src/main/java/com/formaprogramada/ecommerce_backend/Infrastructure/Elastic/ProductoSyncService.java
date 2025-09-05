package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

public interface ProductoSyncService {

    void sincronizarProducto(Integer productoId);

    void sincronizarTodosLosProductos();

    void eliminarDeIndice(Integer productoId);
}
