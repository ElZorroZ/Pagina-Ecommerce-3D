package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaCategoriaArchivoRepository extends JpaRepository<CategoriaArchivoEntity, Integer> {
    @Query("SELECT categoriaId FROM CategoriaArchivoEntity  WHERE categoriaId = :id")
    Optional<CategoriaArchivoEntity> findBycategoriaId(@Param("id") int id);
    Optional<CategoriaArchivoEntity> findByCategoriaId_Id(Integer categoriaId);
}
