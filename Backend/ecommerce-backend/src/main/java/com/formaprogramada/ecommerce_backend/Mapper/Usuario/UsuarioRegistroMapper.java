package com.formaprogramada.ecommerce_backend.Mapper.Usuario;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioRegistroRequest;

public class UsuarioRegistroMapper {
    public static Usuario toDomain(UsuarioRegistroRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setGmail(request.getGmail());
        usuario.setPassword(request.getPassword());
        return usuario;
    }
}
