package com.formaprogramada.ecommerce_backend.Mapper.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import org.springframework.stereotype.Component;
@Component
public class CarritoMapper {

    // Convierte desde request (DTO) a dominio
    public static Carrito toDomain(CarritoAgregarRequest request) {
        Carrito carrito = new Carrito();
        // Aquí no seteamos el id porque es nuevo
        carrito.setProductoId(request.getProductoId());
        carrito.setUsuarioId(request.getUsuarioId());
        carrito.setCantidad(request.getCantidad());
        carrito.setPrecioTotal(request.getPrecioTotal());
        carrito.setPrecioUnitario(request.getPrecioUnitario());
        carrito.setEsDigital(request.getEsDigital());
        carrito.setColor(request.getColor());
        return carrito;
    }

    // Convierte desde entidad JPA a dominio (aquí sí seteamos id)
    public Carrito toDomain2(CarritoEntity entity) {
        if (entity == null) return null;

        Carrito carrito = new Carrito();
        carrito.setId(entity.getId());                    // <-- importante!
        carrito.setProductoId(entity.getProductoId());
        carrito.setUsuarioId(entity.getUsuarioId());
        carrito.setCantidad(entity.getCantidad());
        carrito.setPrecioTotal(entity.getPrecioTotal());
        carrito.setPrecioUnitario(entity.getPrecioUnitario());
        carrito.setEsDigital(entity.isEsDigital());
        carrito.setColor(entity.getColorId());// agregalo también para consistencia
        return carrito;
    }

    // Convierte desde dominio a entidad JPA
    public CarritoEntity toEntity(Carrito carrito) {
        if (carrito == null) {
            return null;
        }

        CarritoEntity.CarritoEntityBuilder builder = CarritoEntity.builder();

        builder.id(carrito.getId());                      // importante para update
        builder.productoId(carrito.getProductoId());
        builder.usuarioId(carrito.getUsuarioId());
        builder.cantidad(carrito.getCantidad());
        builder.precioTotal(carrito.getPrecioTotal());
        builder.precioUnitario(carrito.getPrecioUnitario());
        builder.esDigital(carrito.isEsDigital());
        builder.colorId(carrito.getColor());

        return builder.build();
    }
}
