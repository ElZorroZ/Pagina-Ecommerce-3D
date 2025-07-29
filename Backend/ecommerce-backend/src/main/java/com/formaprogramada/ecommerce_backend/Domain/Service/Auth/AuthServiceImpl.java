package com.formaprogramada.ecommerce_backend.Domain.Service.Auth;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.AuthRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.AuthResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioRegistroMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UsuarioMapper usuarioMapper;

    @Override
    public AuthResponse register(UsuarioRegistroRequest request) {
        Usuario usuario = UsuarioRegistroMapper.toDomain(request);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setPermiso(0);
        usuario.setId(null);
        usuarioRepository.guardar(usuario);

        // Pasamos los claims y username explícitamente
        Map<String, Object> claims = Map.of("permiso", usuario.getPermiso());
        String token = jwtService.generateAccessToken(claims, usuario.getGmail());
        String refreshToken = jwtService.generateRefreshToken(Map.of(), usuario.getGmail());

        return AuthResponse.builder()
                .accessToken(token)
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
        String rol = "";
        if (usuario.getPermiso()==0){
            rol = "CLIENTE";
        } else if (usuario.getPermiso()==1) {
            rol = "ADMIN";
        } else if (usuario.getPermiso()==2) {
            rol = "COLABORADOR";
        }
        Map<String, Object> claims = Map.of("roles", List.of("ROLE_" +  rol));

        String token = jwtService.generateAccessToken(claims, usuario.getGmail());
        String refreshToken = jwtService.generateRefreshToken(claims, usuario.getGmail());

        return AuthResponse.builder()
                .accessToken(token)
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

        String rol = "";
        if (usuario.getPermiso()==0){
            rol = "CLIENTE";
        } else if (usuario.getPermiso()==1) {
            rol = "ADMIN";
        } else if (usuario.getPermiso()==2) {
            rol = "COLABORADOR";

        }
        Map<String, Object> claims = Map.of("roles", List.of("ROLE_" + rol));

        String newToken = jwtService.generateAccessToken(claims, usuario.getGmail());
        String newRefreshToken = jwtService.generateRefreshToken(claims, usuario.getGmail());

        return AuthResponse.builder()
                .accessToken(newToken)
                .refreshToken(newRefreshToken)
                .build();
    }



}
