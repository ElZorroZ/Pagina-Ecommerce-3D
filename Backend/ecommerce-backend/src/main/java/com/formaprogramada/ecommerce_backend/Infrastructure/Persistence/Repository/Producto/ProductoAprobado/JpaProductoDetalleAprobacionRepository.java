package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDetalleAprobacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductoDetalleAprobacionRepository extends JpaRepository<ProductoDetalleAprobacionEntity, Integer> {
    ProductoDetalleAprobacionEntity findByProductoId(Integer productoId);
    void deleteByProductoId(Integer productoId);
}
