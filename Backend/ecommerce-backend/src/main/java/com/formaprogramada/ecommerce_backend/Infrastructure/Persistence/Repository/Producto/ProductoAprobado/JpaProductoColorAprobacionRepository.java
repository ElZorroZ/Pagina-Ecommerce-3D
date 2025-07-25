package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorAprobacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductoColorAprobacionRepository extends JpaRepository<ProductoColorAprobacionEntity, Integer> {
    void deleteByProductoId(Integer productoId);
    List<ProductoColorAprobacionEntity> findByProductoId(Integer productoId);
}
