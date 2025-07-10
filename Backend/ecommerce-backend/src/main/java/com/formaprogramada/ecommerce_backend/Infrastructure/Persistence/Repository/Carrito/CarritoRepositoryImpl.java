package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Carrito.CarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Descuento.CarritoMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class CarritoRepositoryImpl implements CarritoRepository {
    private CarritoMapper carritoMapper;
    private JpaCarritoRepository jpaCarritoRepository;
    @Override
    public Carrito CrearDescuento(Carrito carrito) {
        CarritoEntity entity= carritoMapper.toEntity(carrito);
        CarritoEntity savedentity= jpaCarritoRepository.save(entity);
        return carritoMapper.toDomain2(savedentity);
    }

}
