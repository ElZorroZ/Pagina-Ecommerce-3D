package com.formaprogramada.ecommerce_backend.Mapper.Usuario;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioGetUpdateResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioRegistroRequest;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toDomain(UsuarioEntity entity) {
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

        // Nuevo campo: proveedor
        usuario.setProveedor(entity.getProveedor());

        return usuario;
    }



    public UsuarioEntity toEntity(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setNombre(usuario.getNombre());
        entity.setApellido(usuario.getApellido());
        entity.setGmail(usuario.getGmail());
        entity.setPassword(usuario.getPassword());
        entity.setPermiso(usuario.getPermiso());
        entity.setVerificado(usuario.isVerificado());
        entity.setProveedor(usuario.getProveedor());
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
