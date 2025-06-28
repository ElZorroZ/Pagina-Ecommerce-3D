package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoriaArchivoRepository extends JpaRepository<CategoriaArchivoEntity, Integer> {
}
