package com.formaprogramada.ecommerce_backend.Domain.Service.Jwt;


import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.AuthResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.RefreshToken.JpaRefreshTokenRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Service
@Transactional
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;
    private final UsuarioMapper usuarioMapper;
    private final Logger logger = LoggerFactory.getLogger(JwtTokenServiceImpl.class);

    @Override
    @Transactional
    public AuthResponse generarTokens(UserDetails userDetails) {
        String username = userDetails.getUsername();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> claims = Map.of("roles", roles);

        String accessToken = jwtService.generateAccessToken(claims, username);
        String refreshTokenNuevo = jwtService.generateRefreshToken(claims, username);

        Usuario usuario = usuarioRepository.buscarPorGmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        UsuarioEntity usuarioEntity = usuarioMapper.toEntity(usuario);

        // ✅ Usar Integer - coincide con el tipo del ID de usuario
        jpaRefreshTokenRepository.expireTokensByUsuarioId(usuarioEntity.getId());

        RefreshTokenEntity rtEntity = new RefreshTokenEntity(
                refreshTokenNuevo,
                usuarioEntity,
                LocalDateTime.now().plusDays(7)
        );
        rtEntity.setEstado("VALID");
        jpaRefreshTokenRepository.save(rtEntity);

        logger.info("Tokens generados para usuario: {}", username);
        return new AuthResponse(accessToken, refreshTokenNuevo, usuario.getId());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuthResponse refrescarTokens(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            logger.info("Procesando refresh para usuario: {}", username);

            if (jwtService.isTokenExpired(refreshToken)) {
                logger.warn("Refresh token expirado para usuario: {}", username);
                throw new RefreshTokenExpiredException("Refresh token expirado");
            }

            RefreshTokenEntity tokenEntity = jpaRefreshTokenRepository
                    .findByTokenAndEstadoForUpdate(refreshToken, "VALID")
                    .orElseThrow(() -> {
                        logger.warn("Refresh token no encontrado para usuario: {}", username);
                        return new RefreshTokenNotFoundException("Refresh token no válido");
                    });

            if (!tokenEntity.getUsuario().getGmail().equals(username)) {
                logger.error("Token no pertenece al usuario correcto");
                throw new RefreshTokenNotFoundException("Token no válido para el usuario");
            }

            UsuarioEntity usuarioEntity = tokenEntity.getUsuario();
            Usuario usuario = usuarioMapper.toDomain(usuarioEntity);

            List<String> roles = List.of("ROLE_" + mapearPermiso(usuario.getPermiso()));
            Map<String, Object> claims = Map.of("roles", roles);

            String newAccessToken = jwtService.generateAccessToken(claims, username);
            String newRefreshToken = jwtService.generateRefreshToken(claims, username);

            // Invalidar token actual
            tokenEntity.setEstado("EXPIRED");
            jpaRefreshTokenRepository.save(tokenEntity);

            // Crear nuevo token
            RefreshTokenEntity newTokenEntity = new RefreshTokenEntity(
                    newRefreshToken,
                    usuarioEntity,
                    LocalDateTime.now().plusDays(7)
            );
            newTokenEntity.setEstado("VALID");
            jpaRefreshTokenRepository.save(newTokenEntity);

            logger.info("Refresh exitoso para usuario: {}", username);
            return new AuthResponse(newAccessToken, newRefreshToken, usuario.getId());

        } catch (Exception e) {
            logger.error("Error en refresh para token: {}", e.getMessage());
            throw e;
        }
    }

    private String mapearPermiso(Integer permiso) {
        return switch (permiso) {
            case 0 -> "CLIENTE";
            case 1 -> "ADMIN";
            case 2 -> "COLABORADOR";
            default -> throw new IllegalArgumentException("Permiso inválido: " + permiso);
        };
    }
}