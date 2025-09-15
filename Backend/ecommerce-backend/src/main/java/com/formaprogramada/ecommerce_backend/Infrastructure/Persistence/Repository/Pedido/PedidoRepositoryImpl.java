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
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito.JpaCarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoColorRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Pedido.PedidoMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Base64;
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
    @Autowired
    private JpaProductoColorRepository jpaProductoColorRepository;

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

                // Traer nombre real del producto
                ProductoEntity producto = jpaProductoRepository.findById(productoEntity.getProductoId().getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                productoEntity.setNombre(producto.getNombre());

                // Traer nombre real del color
                ProductoColorEntity color = jpaProductoColorRepository.findById(productoEntity.getColorId())
                        .orElseThrow(() -> new RuntimeException("Color no encontrado"));
                productoEntity.setNombreColor(color.getColor());

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

        // Obtener pedido
        PedidoEntity byId = jpaPedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Obtener productos del pedido
        List<PedidoProductoEntity> pedidoProducto = jpaPedidoProductoRepository.findByPedidoId(byId);

        List<ProductoEnPedidoDTO> productoEnPedidoDTO = new ArrayList<>();
        for (PedidoProductoEntity p : pedidoProducto) {
            ProductoEnPedidoDTO dto = new ProductoEnPedidoDTO();
            dto.setId(p.getId());
            dto.setNombre(p.getNombre());
            dto.setCantidad(p.getCantidad());
            dto.setPrecio(p.getPrecio());
            dto.setEsDigital(p.getEsDigital());
            dto.setColorId(p.getColorId());
            dto.setColorNombre(p.getColor() != null ? p.getColor().getColor() : null);
            dto.setPrecioTotal(p.getPrecio() * p.getCantidad());

            if (p.getProductoId() != null) {
                dto.setProductoId(p.getProductoId().getId());

                ProductoEntity productoReal = jpaProductoRepository.findById(p.getProductoId().getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                if (productoReal.getArchivo() != null) {
                    dto.setArchivoBase64(Base64.getEncoder().encodeToString(productoReal.getArchivo()));
                } else {
                    dto.setArchivoBase64(null);
                }
            } else {
                dto.setProductoId(null);
                dto.setArchivoBase64(null);
            }

            productoEnPedidoDTO.add(dto);
        }

        // Datos del usuario
        UsuarioEntity ue = byId.getUsuarioId();
        UsuarioEntity usuario = jpaUsuarioRepository.findById(ue.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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
        List<PedidoEntity> listaPedidos = jpaPedidoRepository.PedidosDeUsuario(idUsuario);
        List<PedidoDTO> listaDTO = new ArrayList<>();

        for (PedidoEntity pedido : listaPedidos) {
            PedidoDTO pedidoDTO = PedidoMapper.toDTO(pedido); // mapper base

            List<ProductoEnPedidoDTO> productosDTO = new ArrayList<>();
            for (PedidoProductoEntity p : pedido.getProductos()) {
                ProductoEnPedidoDTO dto = new ProductoEnPedidoDTO();
                dto.setId(p.getId());
                dto.setProductoId(p.getProductoId().getId());
                dto.setNombre(p.getNombre());
                dto.setCantidad(p.getCantidad());
                dto.setPrecio(p.getPrecio());
                dto.setEsDigital(p.getEsDigital());
                dto.setColorId(p.getColorId());
                dto.setPrecioTotal(p.getCantidad() * p.getPrecio());
                dto.setHex(p.getColor() != null ? p.getColor().getHex() : null);

                // Traer archivo desde PRODUCTO si es digital
                ProductoEntity productoReal = jpaProductoRepository.findById(p.getProductoId().getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                if (p.getEsDigital() && productoReal.getArchivo() != null) {
                    dto.setArchivoBase64(Base64.getEncoder().encodeToString(productoReal.getArchivo()));
                } else {
                    dto.setArchivoBase64(null);
                }

                // Color: DIGITAL si es digital, sino el color normal
                dto.setColorNombre(p.getEsDigital() ? "DIGITAL" : (p.getColor() != null ? p.getColor().getColor() : null));

                productosDTO.add(dto);
            }

            pedidoDTO.setProductos(productosDTO);
            listaDTO.add(pedidoDTO);
        }

        return listaDTO;
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
