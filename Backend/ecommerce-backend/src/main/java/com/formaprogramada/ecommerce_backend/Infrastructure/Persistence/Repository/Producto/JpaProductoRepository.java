package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductoRepository extends JpaRepository<ProductoEntity, Integer> {
    // Podés agregar métodos personalizados si querés
}

