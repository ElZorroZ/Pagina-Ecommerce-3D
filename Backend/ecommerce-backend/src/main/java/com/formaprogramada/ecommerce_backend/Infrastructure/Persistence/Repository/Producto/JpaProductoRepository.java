package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto;

import com.formaprogramada.ecommerce_backend.Domain.Model.Producto.Producto;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDestacadoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaProductoRepository extends JpaRepository<ProductoEntity, Integer> {
    Page<ProductoEntity> findByCategoriaId_Id(Integer categoriaId, Pageable pageable);
    boolean existsByNombre(String nombre);
    Optional<ProductoEntity> findByNombre(String nombre);
    @Query(value = "CALL sp_obtener_productos_completos()", nativeQuery = true)
    List<Object[]> obtenerProductosCompletosSP();
    @Procedure(procedureName = "listar_productos_por_categoria")
    List<Object[]> listarProductosPorCategoriaSP(
            @Param("p_categoria_id") Integer categoriaId,
            @Param("p_offset") Integer offset,
            @Param("p_limit") Integer limit
    );
    long countByCategoriaId_Id(Integer categoriaId);
    Optional<ProductoEntity> findTopByOrderByIdDesc();
    @Query("SELECT p.id FROM ProductoEntity p")
    List<Integer> findAllIds();

}

