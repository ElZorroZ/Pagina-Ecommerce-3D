package com.formaprogramada.ecommerce_backend.Mapper;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.CategoriaEntity;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface CategoriaEntityMapper {

        CategoriaEntity toEntity(Categoria categoria);

        Categoria toDomainC(CategoriaEntity entity);

    }
