package com.formaprogramada.ecommerce_backend.Domain.Repository.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;

import java.util.List;

public interface PedidoRepository {
    Pedido CrearPedido(List<PedidoProducto> lista, int id);
    void BorrarPedido(int id);
    PedidoUsuarioDTO verPedido(int id);
    List<PedidoDTO> verPedidos();
    void ModificarPedido(UsuarioUpdatePedido usuario);
}
