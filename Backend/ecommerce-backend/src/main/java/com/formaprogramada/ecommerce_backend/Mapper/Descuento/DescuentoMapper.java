package com.formaprogramada.ecommerce_backend.Mapper.Descuento;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Descuento;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Descuento.DescuentoCrearRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Descuento.DescuentoEntity;
import org.springframework.stereotype.Component;

@Component
public class DescuentoMapper {
    public static Descuento toDomain(DescuentoCrearRequest request) {
        Descuento descuento = new Descuento();
        descuento.setNombre(request.getNombre());
        descuento.setDescripcion(request.getDescripcion());
        descuento.setPorcentaje(request.getPorcentaje());
        return descuento;
    }

    public Descuento toDomain2(DescuentoEntity request) {
        Descuento descuento = new Descuento();
        descuento.setNombre(request.getNombre());
        descuento.setDescripcion(request.getDescripcion());
        descuento.setPorcentaje(request.getPorcentaje());
        return descuento;
    }

    public DescuentoEntity toEntity(Descuento descuento) {
        if ( descuento == null ) {
            return null;
        }

        DescuentoEntity.DescuentoEntityBuilder descuentoEntity = DescuentoEntity.builder();

        descuentoEntity.id( descuento.getId() );
        descuentoEntity.nombre( descuento.getNombre() );
        descuentoEntity.descripcion( descuento.getDescripcion() );
        descuentoEntity.porcentaje( descuento.getPorcentaje());

        return descuentoEntity.build();
    }
}
