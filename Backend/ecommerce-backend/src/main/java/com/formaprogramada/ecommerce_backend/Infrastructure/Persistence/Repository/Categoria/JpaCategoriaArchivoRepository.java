package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCategoriaArchivoRepository extends JpaRepository<CategoriaArchivoEntity, Integer> {
    Optional<CategoriaArchivoEntity> findBycategoriaId(int id);
}
