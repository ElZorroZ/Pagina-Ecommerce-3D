package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPedidoProductoRepository extends JpaRepository<PedidoProductoEntity, Integer> {
}
