package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaProductoColorRepository extends JpaRepository<ProductoColorEntity, Integer> {
    List<ProductoColorEntity> findByProductoId(Integer productoId);
    void deleteByProducto_Id(Integer productoId);
}
