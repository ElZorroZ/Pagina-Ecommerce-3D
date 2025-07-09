package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Descuento;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Descuento;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Descuento.DescuentoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Descuento.DescuentoEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Descuento.DescuentoMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class DescuentoRepositoryImpl implements DescuentoRepository {
    private DescuentoMapper descuentoMapper;
    private JpaDescuentoRepository jpaDescuentoRepository;
    @Override
    public Descuento CrearDescuento(Descuento descuento) {
        DescuentoEntity entity=descuentoMapper.toEntity(descuento);
        DescuentoEntity savedentity=jpaDescuentoRepository.save(entity);
        return descuentoMapper.toDomain2(savedentity);
    }
}
