package com.formaprogramada.ecommerce_backend.Security.Hasher;

public interface PasswordHasher {
    String hash(String contraseña);
}
