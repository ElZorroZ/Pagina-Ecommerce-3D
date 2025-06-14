package com.formaprogramada.ecommerce_backend.Infrastructure.Security.Hasher;
import com.formaprogramada.ecommerce_backend.Infrastructure.Security.Hasher.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    @Autowired
    public BCryptPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String contraseña) {
        return encoder.encode(contraseña);
    }
}

