package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDestacadoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaProductoDestacadoRepository extends JpaRepository<ProductoDestacadoEntity, Integer> {

    Optional<ProductoDestacadoEntity> findByProducto(ProductoEntity producto);
    boolean existsByProductoId(Integer productoId);
    boolean existsByProducto(ProductoEntity producto);
    Page<ProductoDestacadoEntity> findAll(Pageable pageable);
    List<ProductoDestacadoEntity> findTop10ByOrderByIdAsc();
    Optional<ProductoDestacadoEntity> findByProductoId(Integer productoId);
}
