package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductoDetalleRepository extends JpaRepository<ProductoDetalleEntity, Integer> {
    // También podés agregar métodos personalizados si lo necesitás
    List<ProductoDetalleEntity> findByProductoId(Integer productoId);
}
