package com.formaprogramada.ecommerce_backend.Mapper.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.ProductoEnPedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PedidoMapper {

    public static List<PedidoProducto> toDomain(List<CarritoCompletoDTO> lista) {
        List<PedidoProducto> listafinal= new ArrayList<>();
        for (CarritoCompletoDTO carrito: lista) {
            PedidoProducto pedidoProducto = new PedidoProducto();
            pedidoProducto.setPedidoId(null);
            pedidoProducto.setProductoId(carrito.getProductoId());
            pedidoProducto.setCantidad(carrito.getCantidad());
            pedidoProducto.setPrecio(carrito.getPrecioUnitario().doubleValue());
            pedidoProducto.setEsDigital(carrito.getEsDigital() != null && carrito.getEsDigital() == 1);
            pedidoProducto.setColorId(carrito.getColorId());
            pedidoProducto.setNombre(carrito.getNombre());

            listafinal.add(pedidoProducto);

        }
            return listafinal;
    }

    public static Pedido toDomain2(List<PedidoProducto> lista, int id){
        Pedido pedido= new Pedido();

        Date fechaActual = new Date();

        pedido.setFechaPedido(fechaActual);
        double total=0;
        for (PedidoProducto pedidoProducto: lista){
            total+= pedidoProducto.getPrecio()*pedidoProducto.getCantidad();
        }
        pedido.setTotal(total);
        pedido.setUsuarioId(id);
        pedido.setEstado("Preparando");

        return  pedido;
    }

    public static PedidoEntity toEntity(Pedido pedido) {

        PedidoEntity pedidoEntity = new PedidoEntity();

        pedidoEntity.setId( pedido.getId() );
        pedidoEntity.setFechaPedido( pedido.getFechaPedido() );
        UsuarioEntity usu= new UsuarioEntity();
        usu.setId(pedido.getUsuarioId() );
        pedidoEntity.setUsuarioId(usu);
        pedidoEntity.setTotal( pedido.getTotal());
        pedidoEntity.setEstado( pedido.getEstado());





        return pedidoEntity;
    }


    public static List<PedidoProductoEntity> toEntity(List<PedidoProducto> lista) {

        List<PedidoProductoEntity> lista2 = new ArrayList<>();
        for (PedidoProducto pedido: lista){
            PedidoProductoEntity.PedidoProductoEntityBuilder pedidoProductoEntity = PedidoProductoEntity.builder();

            pedidoProductoEntity.id(pedido.getId());

            PedidoEntity ped = new PedidoEntity();
            ped.setId(pedido.getPedidoId());
            pedidoProductoEntity.pedidoId(ped);

            ProductoEntity producto = new ProductoEntity();
            producto.setId(pedido.getProductoId());
            pedidoProductoEntity.productoId(producto);

            pedidoProductoEntity.cantidad(pedido.getCantidad());
            pedidoProductoEntity.precio(pedido.getPrecio());
            pedidoProductoEntity.esDigital(pedido.getEsDigital());
            pedidoProductoEntity.colorId(pedido.getColorId());

            // ✅ Setear los nombres desde el DTO
            pedidoProductoEntity.nombre(pedido.getNombre() != null ? pedido.getNombre() : "");
            pedidoProductoEntity.nombreColor(pedido.getColorNombre() != null ? pedido.getColorNombre() : "");

            lista2.add(pedidoProductoEntity.build());
        }
        return lista2;
    }

    public static List<PedidoDTO> toDTO(List<PedidoEntity> lista){
        List<PedidoDTO> lista2 = new ArrayList<>();
        for (PedidoEntity pedido: lista){

            PedidoDTO pedidoDTO= new PedidoDTO();
            pedidoDTO.setId(pedido.getId());
            pedidoDTO.setFechaPedido(pedido.getFechaPedido());
            pedidoDTO.setTotal(pedido.getTotal());
            pedidoDTO.setEstado(pedido.getEstado());

            // ✅ Evitar NPE si usuarioId es null
            pedidoDTO.setUsuarioId(pedido.getUsuarioId() != null ? pedido.getUsuarioId().getId() : null);

            lista2.add(pedidoDTO);
        }
        return lista2;
    }

    public static PedidoDTO toDTO(PedidoEntity pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setId(pedido.getId());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setTotal(pedido.getTotal());
        dto.setEstado(pedido.getEstado());
        dto.setUsuarioId(pedido.getUsuarioId().getId());

        List<ProductoEnPedidoDTO> productos = pedido.getProductos() != null
                ? pedido.getProductos().stream().map(pp -> {
            ProductoEnPedidoDTO ppDTO = new ProductoEnPedidoDTO();
            ppDTO.setId(pp.getId());

            // 🔹 Evitar NullPointerException
            ppDTO.setProductoId(pp.getProductoId() != null ? pp.getProductoId().getId() : null);

            ppDTO.setNombre(pp.getNombre());
            ppDTO.setPrecio(pp.getPrecio());
            ppDTO.setCantidad(pp.getCantidad());
            ppDTO.setEsDigital(pp.getEsDigital());
            ppDTO.setColorId(pp.getColorId());
            ppDTO.setHex(pp.getColor() != null ? pp.getColor().getHex() : null);
            ppDTO.setPrecioTotal(pp.getPrecio() * pp.getCantidad());

            ppDTO.setColorNombre(pp.getEsDigital() ? "DIGITAL" : (pp.getColor() != null ? pp.getColor().getColor() : null));

            // Imagen
            if (pp.getProductoId() != null && pp.getProductoId().getArchivos() != null && !pp.getProductoId().getArchivos().isEmpty()) {
                pp.getProductoId().getArchivos().sort(Comparator.comparingInt(a -> a.getOrden()));
                ppDTO.setImagen(pp.getProductoId().getArchivos().get(0).getLinkArchivo());
            } else {
                ppDTO.setImagen(null);
            }

            // Archivo Base64 solo si digital
            if (pp.getEsDigital() && pp.getProductoId() != null && pp.getProductoId().getArchivo() != null) {
                ppDTO.setArchivoBase64(Base64.getEncoder().encodeToString(pp.getProductoId().getArchivo()));
            } else {
                ppDTO.setArchivoBase64(null);
            }

            return ppDTO;
        }).collect(Collectors.toList())
                : new ArrayList<>();


        dto.setProductos(productos);

        return dto;
    }





    public static List<PedidoDTO> toDTOList(List<PedidoEntity> pedidos) {
        return pedidos.stream().map(PedidoMapper::toDTO).collect(Collectors.toList());
    }

    public static List<ProductoEnPedidoDTO> toProductoEnPedidoDTO(List<PedidoProductoEntity> pedidoProducto) {
        List<ProductoEnPedidoDTO> PEDto = new ArrayList<>();

        for (PedidoProductoEntity productos : pedidoProducto) {
            ProductoEnPedidoDTO dto = new ProductoEnPedidoDTO();

            dto.setId(productos.getId());
            dto.setProductoId(productos.getProductoId().getId());
            dto.setNombre(productos.getNombre());
            dto.setPrecio(productos.getPrecio());
            dto.setCantidad(productos.getCantidad());
            dto.setEsDigital(productos.getEsDigital());
            dto.setColorId(productos.getColorId());
            dto.setColorNombre(productos.getColor() != null ? productos.getColor().getColor() : null);
            dto.setPrecioTotal(productos.getPrecio() * productos.getCantidad());

            // Convertir archivo a Base64
            if (productos.getProductoId().getArchivo() != null) {
                dto.setArchivoBase64(Base64.getEncoder().encodeToString(productos.getProductoId().getArchivo()));
            } else {
                dto.setArchivoBase64(null);
            }

            PEDto.add(dto);
        }

        return PEDto;
    }
    // Convierte un solo PedidoProducto a PedidoProductoEntity
    public static PedidoProductoEntity toEntity(PedidoProducto pedidoProducto) {
        PedidoProductoEntity.PedidoProductoEntityBuilder builder = PedidoProductoEntity.builder();

        builder.id(pedidoProducto.getId());

        ProductoEntity producto = new ProductoEntity();
        producto.setId(pedidoProducto.getProductoId());
        builder.productoId(producto);

        builder.cantidad(pedidoProducto.getCantidad());
        builder.precio(pedidoProducto.getPrecio());
        builder.esDigital(pedidoProducto.getEsDigital());
        builder.colorId(pedidoProducto.getColorId());
        builder.nombre(pedidoProducto.getNombre());

        // Pedido se setea después, cuando ya se guarda
        builder.pedidoId(null);

        return builder.build();
    }

}
