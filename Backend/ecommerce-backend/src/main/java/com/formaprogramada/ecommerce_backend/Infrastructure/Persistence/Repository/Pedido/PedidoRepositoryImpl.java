package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Pedido.PedidoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito.JpaCarritoRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Pedido.PedidoMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@AllArgsConstructor
public class PedidoRepositoryImpl implements PedidoRepository {

    private JpaPedidoRepository jpaPedidoRepository;
    private JpaPedidoProductoRepository jpaPedidoProductoRepository;

    @Override
    public Pedido CrearPedido(List<PedidoProducto> lista, int id) {
        Pedido pedido = PedidoMapper.toDomain2(lista, id);
        PedidoEntity pedido1 = PedidoMapper.toEntity(pedido);
        PedidoEntity saved = jpaPedidoRepository.save(pedido1);

        List<PedidoProductoEntity> lista2 = PedidoMapper.toEntity(lista);
        for (PedidoProductoEntity pedidoProducto : lista2) {
            pedidoProducto.setPedidoId(saved);
            jpaPedidoProductoRepository.save(pedidoProducto);
        }

        return pedido;
    }
}
