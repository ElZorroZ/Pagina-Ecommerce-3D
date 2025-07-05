package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaDestacadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface JpaCategoriaDestacadoRepository extends JpaRepository<CategoriaDestacadoEntity, Integer> {
}
