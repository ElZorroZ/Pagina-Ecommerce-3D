package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Pedido.PedidoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.ProductoEnPedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
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
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class PedidoRepositoryImpl implements PedidoRepository {

    private JpaPedidoRepository jpaPedidoRepository;
    private JpaPedidoProductoRepository jpaPedidoProductoRepository;
    private JpaProductoRepository jpaProductoRepository;
    private JpaUsuarioRepository jpaUsuarioRepository;


    @Override
    public Pedido CrearPedido(List<PedidoProducto> lista, int id) {
        // 1️⃣ Crear el Pedido desde la lista de productos
        Pedido pedido = PedidoMapper.toDomain2(lista, id);

        // 2️⃣ Convertir a entidad
        PedidoEntity pedidoEntity = PedidoMapper.toEntity(pedido);

        // 3️⃣ Guardar la entidad para generar ID
        PedidoEntity savedPedido = jpaPedidoRepository.save(pedidoEntity);

        try {
            // 4️⃣ Mapear la lista completa de productos a entidades y setear el pedido guardado
            List<PedidoProductoEntity> productosEntities = PedidoMapper.toEntity(lista);
            for (PedidoProductoEntity productoEntity : productosEntities) {
                productoEntity.setPedidoId(savedPedido);
                jpaPedidoProductoRepository.save(productoEntity);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al momento de colocar un producto en el pedido", e);
        }

        // 5️⃣ Devolver el Pedido ya con ID generado
        pedido.setId(savedPedido.getId());
        return pedido;
    }



    @Override
    public void BorrarPedido(int id) {
        try {
            jpaPedidoRepository.deleteById(id);
        }catch (Exception e){
            throw new RuntimeException("No se logró borrar el pedido");
    }
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

    @Override
    public List<PedidoDTO> verPedidosDeUsuario(UsuarioEntity idUsuario) {
        List<PedidoEntity> lista= jpaPedidoRepository.PedidosDeUsuario(idUsuario);
        System.out.println(lista);
        return  PedidoMapper.toDTO(lista);
    }

    @Override
    public void ModificarPedido(UsuarioUpdatePedido usuario) {
        if (jpaUsuarioRepository.existsByGmail(usuario.getGmail())){
            jpaUsuarioRepository.actualizarUsuario(usuario.getGmail(), usuario.getNombre(), usuario.getApellido(), usuario.getDireccion(), usuario.getCp(), usuario.getCiudad(), usuario.getTelefono());
        }
        else{
            throw new IllegalArgumentException("No existe un usuario con ese Gmail.");
        }
    }

    @Override
    public void CambiarEstado(String estado, int id) {
        jpaPedidoRepository.modificarEstado(id,estado);

    }


}
