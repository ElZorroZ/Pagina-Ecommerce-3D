package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDetalleAprobacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPedidoProductoRepository extends JpaRepository<PedidoProductoEntity, Integer> {
    List<PedidoProductoEntity> findByPedidoId(PedidoEntity pedidoId);
}
