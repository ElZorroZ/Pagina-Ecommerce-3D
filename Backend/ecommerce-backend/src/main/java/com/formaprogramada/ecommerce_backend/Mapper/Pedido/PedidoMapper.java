package com.formaprogramada.ecommerce_backend.Mapper.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PedidoMapper {

    public static List<PedidoProducto> toDomain(List<CarritoEntity> lista) {
        List<PedidoProducto> listafinal= new ArrayList<>();
        for (CarritoEntity carrito: lista) {
            PedidoProducto pedidoProducto = new PedidoProducto();
            pedidoProducto.setPedidoId(null);
            pedidoProducto.setProductoId(carrito.getProductoId());
            pedidoProducto.setCantidad(carrito.getCantidad());
            pedidoProducto.setPrecio(carrito.getPrecioUnitario());
            pedidoProducto.setEsDigital(carrito.isEsDigital());

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

            pedidoProductoEntity.id( pedido.getId() );
            PedidoEntity ped= new PedidoEntity();
            ProductoEntity producto= new ProductoEntity();
            ped.setId(pedido.getPedidoId() );
            pedidoProductoEntity.pedidoId( ped);
            producto.setId(pedido.getProductoId());
            pedidoProductoEntity.productoId( producto );
            pedidoProductoEntity.cantidad( pedido.getCantidad());
            pedidoProductoEntity.precio( pedido.getPrecio());
            pedidoProductoEntity.esDigital( pedido.getEsDigital());

            lista2.add(pedidoProductoEntity.build());
        }





        return lista2;
    }
}
