package com.formaprogramada.ecommerce_backend.Domain.Service.TokenVerificacion;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.TokenVerificacion.TokenVerificacion;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;

import java.util.Optional;

public interface TokenVerificacionService {
    TokenVerificacion crearTokenParaUsuario(UsuarioEntity usuario);
    Optional<TokenVerificacion> validarToken(String token);
}