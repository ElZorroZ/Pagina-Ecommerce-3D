package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProductoEventListener {

    @Autowired
    private ProductoSyncService productoSyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductoCreado(ProductoCreadoEvent event) {
        try {
            productoSyncService.sincronizarProducto(event.getProductoId());
        } catch (Exception e) {
            System.err.println("Error sincronizando producto creado: " + e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductoActualizado(ProductoActualizadoEvent event) {
        try {
            productoSyncService.sincronizarProducto(event.getProductoId());
        } catch (Exception e) {
            System.err.println("Error sincronizando producto actualizado: " + e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductoEliminado(ProductoEliminadoEvent event) {
        try {
            productoSyncService.eliminarDeIndice(event.getProductoId());
        } catch (Exception e) {
            System.err.println("Error eliminando producto del índice: " + e.getMessage());
        }
    }
}

