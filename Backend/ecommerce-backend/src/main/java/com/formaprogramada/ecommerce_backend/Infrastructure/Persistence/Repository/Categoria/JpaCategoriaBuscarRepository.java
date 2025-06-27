package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoriaBuscarRepository extends JpaRepository<CategoriaEntity, Long> {

}
