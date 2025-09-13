package com.formaprogramada.ecommerce_backend.Domain.Service.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;

import java.util.List;

public interface PedidoService {

    Pedido CrearPedido(List<PedidoProducto> lista, int id);
    void BorrarPedido(int id);
    PedidoUsuarioDTO verPedido(int id);
    List<PedidoDTO> verPedidos();
    List<PedidoDTO> verPedidosDeUsuario(int id);
    void ModificarPedido(UsuarioUpdatePedido usuario);
    void CambiarEstado(String estado,int id);
    Pedido obtenerPedidoPorMercadoPagoId(String pedidoId);
    List<String> EnviarPedidoOnline(int idPedido);

    }
