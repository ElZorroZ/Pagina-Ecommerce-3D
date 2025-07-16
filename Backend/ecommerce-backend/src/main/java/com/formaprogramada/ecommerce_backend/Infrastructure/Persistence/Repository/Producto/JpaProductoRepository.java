package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;

import com.formaprogramada.ecommerce_backend.Domain.Model.Producto.Producto;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductoRepository extends JpaRepository<ProductoEntity, Integer> {
    Page<ProductoEntity> findByCategoriaId_Id(Integer categoriaId, Pageable pageable);
}

