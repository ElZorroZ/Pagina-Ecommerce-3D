package com.formaprogramada.ecommerce_backend.Domain.Service.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Pedido.PedidoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PedidoServiceImpl implements PedidoService {
    private PedidoRepository pedidoRepository;
    @Override
    public Pedido CrearPedido(List<PedidoProducto> lista, int id) {
        return pedidoRepository.CrearPedido(lista, id);

    }

    @Override
    public void BorrarPedido(int id) {
        pedidoRepository.BorrarPedido(id);
    }

    @Override
    public PedidoUsuarioDTO verPedido(int id) {
        return pedidoRepository.verPedido(id);
    }

    @Override
    public List<PedidoDTO> verPedidos() {
        return pedidoRepository.verPedidos();
    }

    @Override
    public List<PedidoDTO> verPedidosDeUsuario(int id) {
        UsuarioEntity idUsuario = new UsuarioEntity();
        idUsuario.setId(id);
        return pedidoRepository.verPedidosDeUsuario(idUsuario);
    }

    @Override
    public void ModificarPedido(UsuarioUpdatePedido usuario) {
        pedidoRepository.ModificarPedido(usuario);
    }

    @Override
    public void CambiarEstado(String estado,int id) {
        pedidoRepository.CambiarEstado(estado, id);
    }
}
