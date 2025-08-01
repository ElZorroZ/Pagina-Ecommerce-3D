package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface JpaProductoAprobacionRepository extends JpaRepository<ProductoAprobacionEntity, Integer> {
    List<ProductoAprobacionEntity>  findByUsuarioId_Id(int id);
    Optional<ProductoAprobacionEntity> findByNombre(String nombre);
    @Query("SELECT p.id FROM ProductoAprobacionEntity p")
    List<Integer> findAllIds();
    Optional<ProductoAprobacionEntity> findTopByOrderByIdDesc();
}