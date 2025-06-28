package com.formaprogramada.ecommerce_backend.Domain.Service.Jwt;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}