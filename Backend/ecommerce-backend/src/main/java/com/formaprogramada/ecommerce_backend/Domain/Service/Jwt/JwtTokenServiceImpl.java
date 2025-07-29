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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;
    private final UsuarioMapper usuarioMapper;
    private final Logger logger = LoggerFactory.getLogger(JwtTokenServiceImpl.class); // poné la clase correcta

    @Override
    public AuthResponse generarTokens(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> claims = Map.of("roles", roles);
        String username = userDetails.getUsername();

        String accessToken = jwtService.generateAccessToken(claims, username);
        String refreshTokenNuevo = jwtService.generateRefreshToken(claims, username);

        Usuario usuario = usuarioRepository.buscarPorGmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        UsuarioEntity usuarioEntity = usuarioMapper.toEntity(usuario);

        List<RefreshTokenEntity> tokensActivos = jpaRefreshTokenRepository.findAllByUsuarioIdAndEstado(usuarioEntity.getId(), "VALID");

        if (!tokensActivos.isEmpty()) {
            RefreshTokenEntity rtEntity = tokensActivos.get(0);
            rtEntity.setToken(refreshTokenNuevo);
            rtEntity.setFechaExpiracion(LocalDateTime.now().plusSeconds(7 * 24 * 60 * 60));
            rtEntity.setEstado("VALID");
            jpaRefreshTokenRepository.save(rtEntity);
        } else {
            RefreshTokenEntity rtEntity = new RefreshTokenEntity(refreshTokenNuevo, usuarioEntity, LocalDateTime.now().plusSeconds(7 * 24 * 60 * 60));
            rtEntity.setEstado("VALID");
            jpaRefreshTokenRepository.save(rtEntity);
        }

        // Print de los tokens nuevos
        System.out.println("Tokens generados:");
        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshTokenNuevo);

        return new AuthResponse(accessToken, refreshTokenNuevo);
    }




    @Override
    public synchronized AuthResponse refrescarTokens(String refreshToken) {
        logger.info("Intentando refrescar token");

        if (jwtService.isTokenExpired(refreshToken)) {
            logger.warn("Refresh token expirado: {}", refreshToken);
            throw new RefreshTokenExpiredException("Refresh token expirado");
        }

        // Buscar el refresh token válido
        RefreshTokenEntity tokenEntity = jpaRefreshTokenRepository.findByTokenAndEstado(refreshToken, "VALID")
                .orElseThrow(() -> {
                    logger.warn("Refresh token no encontrado o no válido: {}", refreshToken);
                    return new RefreshTokenNotFoundException("Refresh token no encontrado o inválido");
                });

        String username = jwtService.extractUsername(refreshToken);
        UsuarioEntity usuarioEntity = tokenEntity.getUsuario();
        Usuario usuario = usuarioMapper.toDomain(usuarioEntity);

        String rol = "";
        if (usuario.getPermiso()==0){
            rol = "CLIENTE";
        } else if (usuario.getPermiso()==1) {
            rol = "ADMIN";
        } else if (usuario.getPermiso()==2) {
            rol = "COLABORADOR";

        }
        Map<String, Object> claims = Map.of("roles", List.of("ROLE_" + rol));

        String newAccessToken = jwtService.generateAccessToken(claims, username);
        String newRefreshToken = jwtService.generateRefreshToken(claims, username);

        // Guardar el nuevo token
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusSeconds(7 * 24 * 60 * 60);
        RefreshTokenEntity newTokenEntity = new RefreshTokenEntity(newRefreshToken, usuarioEntity, fechaExpiracion);
        newTokenEntity.setEstado("VALID");
        jpaRefreshTokenRepository.save(newTokenEntity);
        logger.info("Nuevo refresh token generado y guardado para usuario: {}", username);

        // Eliminar el token viejo después de guardar el nuevo
        tokenEntity.setEstado("EXPIRED");
        jpaRefreshTokenRepository.save(tokenEntity);
        logger.info("Refresh token eliminado: {}", refreshToken);

        // Ahora respondé con el nuevo
        return new AuthResponse(newAccessToken, newRefreshToken);

    }

}

