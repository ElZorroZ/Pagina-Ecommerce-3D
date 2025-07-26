package com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.RefreshToken.JpaRefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupService {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCleanupService.class);

    @Scheduled(cron = "0 0 3 * * ?") // Todos los días a las 3 AM
    @Transactional
    public void limpiarTokensExpirados() {
        List<RefreshTokenEntity> expirados = jpaRefreshTokenRepository
                .findAllByEstadoAndFechaExpiracionBefore("EXPIRED", LocalDateTime.now());

        if (!expirados.isEmpty()) {
            jpaRefreshTokenRepository.deleteAll(expirados);
            logger.info("✅ Se eliminaron {} refresh tokens expirados", expirados.size());
        } else {
            logger.info("ℹ️ No hay refresh tokens expirados para eliminar");
        }
    }
}

