package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Service.UsuarioService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.AuthResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.AuthRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.RefreshTokenRequest;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Mapper.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid; // o javax.validation.Valid según tus dependencias
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;      // Servicio para registrar usuarios
    private final AuthenticationManager authManager;  // Para autenticar username/password
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioRegistroRequest request) {
        if (usuarioService.existePorGmail(request.getGmail())) {
            return ResponseEntity.badRequest().body("El gmail ya está registrado");
        }
        System.out.println("Request recibido: " + request);
        var usuario = UsuarioMapper.toDomain(request);
        usuario.setPermiso(false); // cliente por defecto
        usuarioService.registrarUsuario(usuario);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.getGmail(), request.getPassword());
            var auth = authManager.authenticate(authToken);

            var userDetails = (UserDetails) auth.getPrincipal();

            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", userDetails.getAuthorities());

            String accessToken = jwtService.generateAccessToken(claims, userDetails.getUsername());
            String refreshToken = jwtService.generateRefreshToken(Map.of(), userDetails.getUsername());

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (jwtService.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expirado");
        }

        String username = jwtService.extractUsername(refreshToken);
        String accessToken = jwtService.generateAccessToken(Map.of(), username);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }
}