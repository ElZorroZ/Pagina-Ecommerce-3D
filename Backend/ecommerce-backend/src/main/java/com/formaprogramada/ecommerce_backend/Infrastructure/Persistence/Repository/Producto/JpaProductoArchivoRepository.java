package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductoArchivoRepository extends JpaRepository<ProductoArchivoEntity, Integer> {
    List<ProductoArchivoEntity> findByProductoIdOrderByOrdenAsc(Integer productoId);
}

