package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
}
