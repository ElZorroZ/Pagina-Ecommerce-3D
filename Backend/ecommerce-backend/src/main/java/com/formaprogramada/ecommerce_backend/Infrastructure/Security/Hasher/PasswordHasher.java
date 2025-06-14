package com.formaprogramada.ecommerce_backend.Infrastructure.Security.Hasher;

public interface PasswordHasher {
    String hash(String contraseña);
}
