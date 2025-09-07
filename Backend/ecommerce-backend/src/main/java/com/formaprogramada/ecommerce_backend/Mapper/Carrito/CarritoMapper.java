package com.formaprogramada.ecommerce_backend.Mapper.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoColorRepository;
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
        carrito.setColorId(request.getColorId());
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

        // Obtenemos el colorId desde la relación, si existe
        carrito.setColorId(entity.getColor() != null ? entity.getColor().getId() : null);

        return carrito;
    }


    // Convierte desde dominio a entidad JPA
    public CarritoEntity toEntity(Carrito carrito, JpaProductoColorRepository colorRepo) {
        if (carrito == null) return null;

        CarritoEntity.CarritoEntityBuilder builder = CarritoEntity.builder();

        builder.id(carrito.getId());
        builder.productoId(carrito.getProductoId());
        builder.usuarioId(carrito.getUsuarioId());
        builder.cantidad(carrito.getCantidad());
        builder.precioTotal(carrito.getPrecioTotal());
        builder.precioUnitario(carrito.getPrecioUnitario());
        builder.esDigital(carrito.getEsDigital());

        // Setear la relación color correctamente
        if (carrito.getColorId() != null && carrito.getColorId() > 0) {
            ProductoColorEntity colorEntity = colorRepo.findById(carrito.getColorId())
                    .orElseThrow(() -> new IllegalArgumentException("Color no encontrado"));
            builder.color(colorEntity); // esto ya guarda colorId en la DB
        } else {
            builder.color(null);
        }

        return builder.build();
    }


}
