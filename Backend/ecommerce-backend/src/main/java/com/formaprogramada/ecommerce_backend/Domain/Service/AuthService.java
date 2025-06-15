package com.formaprogramada.ecommerce_backend.Domain.Service;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.AuthResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.AuthRequest;

public interface AuthService {
    AuthResponse register(UsuarioRegistroRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse refreshToken(String refreshToken);
}
