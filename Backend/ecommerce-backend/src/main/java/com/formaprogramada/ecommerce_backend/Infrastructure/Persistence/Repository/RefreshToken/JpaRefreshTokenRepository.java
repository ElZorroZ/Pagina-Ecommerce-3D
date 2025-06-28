package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.RefreshToken;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
    Optional<RefreshTokenEntity> findByTokenAndEstado(String token, String estado);
    List<RefreshTokenEntity> findAllByUsuarioIdAndEstado(Integer usuarioId, String estado);

}
