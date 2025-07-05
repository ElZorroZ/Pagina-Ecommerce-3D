package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaDestacadoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface JpaCategoriaDestacadoRepository extends JpaRepository<CategoriaDestacadoEntity, Integer> {
    Optional<CategoriaDestacadoEntity> findByCategoria(CategoriaEntity categoria);

}
