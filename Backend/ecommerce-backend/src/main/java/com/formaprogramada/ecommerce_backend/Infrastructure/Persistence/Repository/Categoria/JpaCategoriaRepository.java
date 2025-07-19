package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JpaCategoriaRepository extends JpaRepository<CategoriaEntity, Integer> {
    Optional<CategoriaEntity> findByNombreIgnoreCase(String nombre);

    @Query("select c.id from CategoriaEntity c")
    List<Integer> findAllIds();
}
