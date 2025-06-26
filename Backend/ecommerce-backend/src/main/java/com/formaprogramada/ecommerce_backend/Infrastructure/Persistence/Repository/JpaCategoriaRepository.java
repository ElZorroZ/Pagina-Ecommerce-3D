package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
public interface JpaCategoriaRepository extends JpaRepository<CategoriaEntity, Integer> {

}
