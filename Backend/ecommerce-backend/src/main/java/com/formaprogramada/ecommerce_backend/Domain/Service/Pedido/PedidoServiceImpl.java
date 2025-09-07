package com.formaprogramada.ecommerce_backend.Domain.Service.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Pedido.PedidoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido.JpaPedidoRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PedidoServiceImpl implements PedidoService {
    private PedidoRepository pedidoRepository;
    @Autowired
    private JpaPedidoRepository jpaPedidoRepository;
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
    public Pedido obtenerPedidoPorMercadoPagoId(String pedidoId) {
        try {
            int id = Integer.parseInt(pedidoId);
            PedidoEntity entity = jpaPedidoRepository.findById(id).orElse(null);
            if (entity == null) return null;

            Pedido pedido = new Pedido();
            pedido.setId(entity.getId());
            pedido.setFechaPedido(entity.getFechaPedido());
            pedido.setTotal(entity.getTotal());
            pedido.setUsuarioId(entity.getUsuarioId().getId()); // <-- aquí
            pedido.setEstado(entity.getEstado());

            return pedido;

        } catch (NumberFormatException e) {
            return null;
        }
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
