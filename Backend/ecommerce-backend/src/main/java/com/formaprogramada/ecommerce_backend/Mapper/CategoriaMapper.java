package com.formaprogramada.ecommerce_backend.Mapper;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CategoriaCreacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.UsuarioEntity;

public class CategoriaMapper {
    public static Categoria toDomainCategoria1(CategoriaCreacionRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return categoria;
    }

    public static Categoria toDomainCategoria2(CategoriaEntity entity) {
        Categoria categoria = new Categoria();
        categoria.setId(entity.getId());
        categoria.setNombre(entity.getNombre());
        categoria.setDescripcion(entity.getDescripcion());
        return usuario;
    }

    public static CategoriaEntity toEntity(Categoria categoria) {
        CategoriaEntity entity = new CategoriaEntity();
        entity.setId(categoria.getId());
        entity.setNombre(categoria.getNombre());
        entity.setDescripcion(categoria.getDescripcion());
        return entity;
    }
}
