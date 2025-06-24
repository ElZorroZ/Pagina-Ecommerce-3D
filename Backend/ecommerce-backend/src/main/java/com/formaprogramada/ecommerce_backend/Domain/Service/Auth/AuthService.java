package com.formaprogramada.ecommerce_backend.Domain.Service.Auth;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.AuthResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.AuthRequest;

public interface AuthService {
    AuthResponse register(UsuarioRegistroRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse refreshToken(String refreshToken);
}
