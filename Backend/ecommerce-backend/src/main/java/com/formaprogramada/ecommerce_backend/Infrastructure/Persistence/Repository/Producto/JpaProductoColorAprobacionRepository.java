package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductoColorAprobacionRepository extends JpaRepository<ProductoColorAprobacionEntity, Integer> {
    void deleteByProducto_Id(Integer productoId);
    List<ProductoColorAprobacionEntity> findByProductoId(Integer productoId);
}
