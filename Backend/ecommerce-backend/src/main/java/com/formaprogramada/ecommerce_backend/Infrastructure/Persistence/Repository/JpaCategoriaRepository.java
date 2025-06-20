package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoriaRepository extends JpaRepository<CategoriaEntity, Integer> {
    boolean existsByNombre(String nombre);
}
