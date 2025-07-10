package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCarritoRepository extends JpaRepository<CarritoEntity, Integer> {
}
