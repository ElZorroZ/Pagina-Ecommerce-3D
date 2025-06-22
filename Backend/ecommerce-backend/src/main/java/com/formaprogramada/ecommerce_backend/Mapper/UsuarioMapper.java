package com.formaprogramada.ecommerce_backend.Mapper;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioGetUpdateResponse;
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
        usuario.setPermiso(entity.getPermiso());
        usuario.setVerificado(entity.isVerificado());

        // Agregamos los nuevos campos
        usuario.setDireccion(entity.getDireccion());
        usuario.setCp(entity.getCp());
        usuario.setCiudad(entity.getCiudad());
        usuario.setTelefono(entity.getTelefono());
        return usuario;
    }

    public static UsuarioEntity toEntity(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setNombre(usuario.getNombre());
        entity.setApellido(usuario.getApellido());
        entity.setGmail(usuario.getGmail());
        entity.setPassword(usuario.getPassword());
        entity.setPermiso(usuario.getPermiso());
        entity.setVerificado(usuario.isVerificado());

        // Agregamos los nuevos campos
        entity.setDireccion(usuario.getDireccion());
        entity.setCp(usuario.getCp());
        entity.setCiudad(usuario.getCiudad());
        entity.setTelefono(usuario.getTelefono());
        return entity;
    }

    public static UsuarioGetUpdateResponse toGetUpdateResponseFromDomain(Usuario usuario) {
        UsuarioGetUpdateResponse dto = new UsuarioGetUpdateResponse();
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setGmail(usuario.getGmail());
        dto.setDireccion(usuario.getDireccion());
        dto.setCp(usuario.getCp());
        dto.setCiudad(usuario.getCiudad());
        dto.setTelefono(usuario.getTelefono());
        return dto;
    }


}
