package com.formaprogramada.ecommerce_backend.Mapper;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;


public class UsuarioMapper {

    public static Usuario toDomain(UsuarioRegistroRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setGmail(request.getGmail());
        usuario.setPassword(request.getPassword());
        return usuario;
    }

    public static Usuario toDomain(UsuarioEntity entity) {
        Usuario usuario = new Usuario();
        usuario.setId(entity.getId());
        usuario.setNombre(entity.getNombre());
        usuario.setApellido(entity.getApellido());
        usuario.setGmail(entity.getGmail());
        usuario.setPassword(entity.getPassword());
        return usuario;
    }

    public static UsuarioEntity toEntity(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setNombre(usuario.getNombre());
        entity.setApellido(usuario.getApellido());
        entity.setGmail(usuario.getGmail());
        entity.setPassword(usuario.getPassword());
        return entity;
    }
}
