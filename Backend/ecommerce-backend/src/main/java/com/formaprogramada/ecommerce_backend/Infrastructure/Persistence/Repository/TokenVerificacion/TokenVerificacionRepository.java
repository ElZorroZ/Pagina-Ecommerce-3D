package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.TokenVerificacion;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.TokenVerificacion.TokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacion, Long> {
    Optional<TokenVerificacion> findByToken(String token);
}

