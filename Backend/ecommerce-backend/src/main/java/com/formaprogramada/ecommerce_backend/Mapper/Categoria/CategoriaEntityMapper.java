package com.formaprogramada.ecommerce_backend.Mapper.Categoria;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaEntityMapper {
    CategoriaEntity toEntity(Categoria categoria);

    Categoria toDomain(CategoriaEntity categoriaEntity);




}
