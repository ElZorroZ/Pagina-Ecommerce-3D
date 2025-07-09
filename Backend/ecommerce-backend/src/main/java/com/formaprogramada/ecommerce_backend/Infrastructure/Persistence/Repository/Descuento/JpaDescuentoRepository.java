package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Descuento;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Descuento.DescuentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDescuentoRepository extends JpaRepository<DescuentoEntity, Integer> {
}
