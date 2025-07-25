package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductoArchivoAprobadoRepository extends JpaRepository<ProductoArchivoAprobacionEntity, Integer> {
    List<ProductoArchivoAprobacionEntity> findByProductoIdOrderByOrdenAsc(Integer productoId);
}
