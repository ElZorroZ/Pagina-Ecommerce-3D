package com.formaprogramada.ecommerce_backend.Mapper;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CategoriaCrearRequest;


public class CategoriaMapper {

    public static Categoria toDomain(CategoriaCrearRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return categoria;
    }
}
