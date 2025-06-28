package com.formaprogramada.ecommerce_backend.Domain.Service.Jwt;

public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}
