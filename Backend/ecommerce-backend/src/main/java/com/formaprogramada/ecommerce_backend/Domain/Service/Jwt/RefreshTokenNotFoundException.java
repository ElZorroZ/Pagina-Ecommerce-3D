package com.formaprogramada.ecommerce_backend.Domain.Service.Jwt;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
