package com.formaprogramada.ecommerce_backend.Mapper.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import org.springframework.stereotype.Component;

@Component
public class CarritoMapper {
    public static Carrito toDomain(CarritoAgregarRequest request) {
        Carrito carrito = new Carrito();
        carrito.setProductoId(request.getProductoId());
        carrito.setUsuarioId(request.getUsuarioId());
        carrito.setCantidad(request.getCantidad());
        carrito.setPrecioTotal(request.getPrecioTotal());
        carrito.setPrecioUnitario(request.getPrecioUnitario());
        carrito.setEsDigital(request.getEsDigital());
        return carrito;
    }

    public Carrito toDomain2(CarritoEntity request) {
        Carrito carrito = new Carrito();
        carrito.setProductoId(request.getProductoId());
        carrito.setUsuarioId(request.getUsuarioId());
        carrito.setCantidad(request.getCantidad());
        carrito.setPrecioTotal(request.getPrecioTotal());
        carrito.setPrecioUnitario(request.getPrecioUnitario());
        return carrito;
    }

    public CarritoEntity toEntity(Carrito carrito) {
        if ( carrito == null ) {
            return null;
        }

        CarritoEntity.CarritoEntityBuilder carritoEntity = CarritoEntity.builder();

        carritoEntity.id( carrito.getId() );
        carritoEntity.productoId( carrito.getProductoId() );
        carritoEntity.usuarioId( carrito.getUsuarioId() );
        carritoEntity.cantidad( carrito.getCantidad());
        carritoEntity.precioTotal( carrito.getPrecioTotal());
        carritoEntity.precioUnitario( carrito.getPrecioUnitario());
        carritoEntity.esDigital(carrito.isEsDigital());



        return carritoEntity.build();
    }
}
