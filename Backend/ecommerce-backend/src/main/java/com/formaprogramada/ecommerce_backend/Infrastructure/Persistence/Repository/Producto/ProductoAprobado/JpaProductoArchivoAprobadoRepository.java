package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JpaProductoArchivoAprobadoRepository extends JpaRepository<ProductoArchivoAprobacionEntity, Integer> {
    List<ProductoArchivoAprobacionEntity> findByProductoIdOrderByOrdenAsc(Integer productoId);
    List<ProductoArchivoAprobacionEntity> findByProducto_IdOrderByOrdenAsc(Integer productoId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ProductoArchivoAprobacionEntity p WHERE p.producto.id = :productoId")
    void deleteByProductoId(@Param("productoId") Integer productoId);
}
