package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

import com.formaprogramada.ecommerce_backend.Domain.Model.Producto.Producto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class ProductoSyncServiceNoop implements  ProductoSyncService {

    @Override
    public void sincronizarProducto(Integer productoId) {
        // No hace nada en dev
    }

    @Override
    public void sincronizarTodosLosProductos() {
        // No hace nada en dev
    }

    @Override
    public void eliminarDeIndice(Integer productoId) {
        // No hace nada en dev
    }
}

