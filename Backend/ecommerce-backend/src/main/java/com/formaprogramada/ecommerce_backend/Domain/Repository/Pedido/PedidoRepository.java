package com.formaprogramada.ecommerce_backend.Domain.Repository.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;

import java.util.List;

public interface PedidoRepository {
    Pedido CrearPedido(List<PedidoProducto> lista, int id);
}
