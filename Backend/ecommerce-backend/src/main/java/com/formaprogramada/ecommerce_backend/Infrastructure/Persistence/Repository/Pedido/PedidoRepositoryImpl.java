package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Pedido.PedidoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.ProductoEnPedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito.JpaCarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Pedido.PedidoMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
@AllArgsConstructor
public class PedidoRepositoryImpl implements PedidoRepository {

    private JpaPedidoRepository jpaPedidoRepository;
    private JpaPedidoProductoRepository jpaPedidoProductoRepository;
    private JpaProductoRepository jpaProductoRepository;
    private JpaUsuarioRepository jpaUsuarioRepository;

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

    @Override
    public void BorrarPedido(int id) {
        jpaPedidoRepository.deleteById(id);
    }

    @Override
    public PedidoUsuarioDTO verPedido(int id) {
        PedidoUsuarioDTO puDTO = new PedidoUsuarioDTO();

        PedidoEntity byId = jpaPedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        List <PedidoProductoEntity> pedidoProducto = jpaPedidoProductoRepository.findByPedidoId(byId);

        List<ProductoEnPedidoDTO> productoEnPedidoDTO = PedidoMapper.toProductoEnPedidoDTO(pedidoProducto);

        UsuarioEntity ue = byId.getUsuarioId();
        System.out.println(ue.getId());
        UsuarioEntity usuario = jpaUsuarioRepository.findById(ue.getId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        puDTO.setFechaPedido(byId.getFechaPedido());
        puDTO.setEstado(byId.getEstado());
        puDTO.setProductos(productoEnPedidoDTO);
        puDTO.setNombre(usuario.getNombre());
        puDTO.setApellido(usuario.getApellido());
        puDTO.setGmail(usuario.getGmail());
        puDTO.setDireccion(usuario.getDireccion());
        puDTO.setCp(usuario.getCp());
        puDTO.setCiudad(usuario.getCiudad());
        puDTO.setTelefono(usuario.getTelefono());
        puDTO.setTotal(byId.getTotal());


        return puDTO;
    }

    @Override
    public List<PedidoDTO> verPedidos() {
         List<PedidoEntity> lista= jpaPedidoRepository.findAll();


        return  PedidoMapper.toDTO(lista);
    }
}
