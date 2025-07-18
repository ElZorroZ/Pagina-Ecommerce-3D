package com.formaprogramada.ecommerce_backend.Mapper.Usuario;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioActualizarMapper {

    public Usuario toDomain(UsuarioUpdatePedido entity) {
        Usuario usuario = new Usuario();
        usuario.setNombre(entity.getNombre());
        usuario.setApellido(entity.getApellido());
        usuario.setGmail(entity.getGmail());

        usuario.setDireccion(entity.getDireccion());
        usuario.setCp(entity.getCp());
        usuario.setCiudad(entity.getCiudad());
        usuario.setTelefono(entity.getTelefono());
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

        entity.setDireccion(usuario.getDireccion());
        entity.setCp(usuario.getCp());
        entity.setCiudad(usuario.getCiudad());
        entity.setTelefono(usuario.getTelefono());
        return entity;
    }
}
