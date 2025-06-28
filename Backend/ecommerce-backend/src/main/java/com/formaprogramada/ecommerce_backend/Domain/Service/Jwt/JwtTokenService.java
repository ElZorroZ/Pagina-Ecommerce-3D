package com.formaprogramada.ecommerce_backend.Domain.Service.Jwt;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.AuthResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtTokenService {
    AuthResponse generarTokens(UserDetails userDetails);
    AuthResponse refrescarTokens(String refreshToken);
}
