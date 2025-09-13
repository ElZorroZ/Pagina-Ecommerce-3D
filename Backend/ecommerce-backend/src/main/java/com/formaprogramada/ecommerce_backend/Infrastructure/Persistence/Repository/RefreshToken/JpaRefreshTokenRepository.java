package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.RefreshToken;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    // ✅ Método con lock pesimista - CON @Query explícita
    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.token = :token AND rt.estado = :estado")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshTokenEntity> findByTokenAndEstadoForUpdate(
            @Param("token") String token,
            @Param("estado") String estado
    );

    // ✅ Invalidación masiva - CON @Query explícita y @Modifying
    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenEntity rt SET rt.estado = 'EXPIRED' WHERE rt.usuario.id = :usuarioId AND rt.estado = 'VALID'")
    void expireTokensByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // ✅ Métodos derivados simples (Spring los auto-genera)
    List<RefreshTokenEntity> findAllByUsuarioIdAndEstado(Integer usuarioId, String estado);
    Optional<RefreshTokenEntity> findByTokenAndEstado(String token, String estado);

    // ✅ Para limpieza - método derivado que SÍ funciona
    List<RefreshTokenEntity> findAllByEstadoAndFechaExpiracionBefore(String estado, LocalDateTime fecha);
}