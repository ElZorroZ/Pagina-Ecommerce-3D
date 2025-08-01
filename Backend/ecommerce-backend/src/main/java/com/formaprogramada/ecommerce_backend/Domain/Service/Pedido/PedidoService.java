package com.formaprogramada.ecommerce_backend.Domain.Service.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;

import java.util.List;

public interface PedidoService {

    Pedido CrearPedido(List<PedidoProducto> lista, int id);

}
