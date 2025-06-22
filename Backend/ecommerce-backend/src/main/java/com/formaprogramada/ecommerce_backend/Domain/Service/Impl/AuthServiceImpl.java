package com.formaprogramada.ecommerce_backend.Domain.Service.Impl;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.AuthService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.AuthRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.AuthResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Mapper.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(UsuarioRegistroRequest request) {
        Usuario usuario = UsuarioMapper.toDomain(request);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setPermiso(false);
        usuario.setId(null);
        usuarioRepository.guardar(usuario);

        // Pasamos los claims y username explícitamente
        Map<String, Object> claims = Map.of("permiso", usuario.getPermiso());
        String token = jwtService.generateAccessToken(claims, usuario.getGmail());
        String refreshToken = jwtService.generateRefreshToken(Map.of(), usuario.getGmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getGmail(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository
                .buscarPorGmail(request.getGmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        Map<String, Object> claims = Map.of("permiso", usuario.getPermiso());
        String token = jwtService.generateAccessToken(claims, usuario.getGmail());
        String refreshToken = jwtService.generateRefreshToken(Map.of(), usuario.getGmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String gmail = jwtService.extractUsername(refreshToken);
        Usuario usuario = usuarioRepository
                .buscarPorGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (!jwtService.isTokenValid(refreshToken, usuario.getGmail())) {
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }

        Map<String, Object> claims = Map.of("permiso", usuario.getPermiso());
        String newToken = jwtService.generateAccessToken(claims, usuario.getGmail());

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(refreshToken)
                .build();
    }


}
