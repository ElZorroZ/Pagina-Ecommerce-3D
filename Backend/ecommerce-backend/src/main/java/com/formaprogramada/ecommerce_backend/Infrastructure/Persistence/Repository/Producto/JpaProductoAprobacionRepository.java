package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface JpaProductoAprobacionRepository extends JpaRepository<ProductoAprobacionEntity, Integer> {
}