package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Service.EmailService;
import com.formaprogramada.ecommerce_backend.Domain.Service.TokenVerificacionService;
import com.formaprogramada.ecommerce_backend.Domain.Service.UsuarioService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.AuthResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.AuthRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.RefreshTokenRequest;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Mapper.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
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
    private final TokenVerificacionService tokenService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioRegistroRequest request) {
        if (usuarioService.existePorGmail(request.getGmail())) {
            return ResponseEntity.badRequest().body("El gmail ya está registrado");
        }

        var usuario = UsuarioMapper.toDomain(request);
        usuario.setPermiso(false);
        usuario.setVerificado(false);

        usuario = usuarioService.registrarUsuario(usuario);

        // Crear token y enviarlo por mail (simulado acá)
        var usuarioEntity = UsuarioMapper.toEntity(usuario);
        var token = tokenService.crearTokenParaUsuario(usuarioEntity);

        // Construir URL de validación (ajustá el frontend según corresponda)
        String urlValidacion = "http://localhost:5500/WEB/usuario/validacion/validar-email.html?token=" + token.getToken();

        // Variables para plantilla Thymeleaf
        Map<String, Object> variables = Map.of(
                "nombre", usuario.getNombre(),         // si tenés nombre, sino cambia o elimina
                "urlValidacion", urlValidacion
        );

        // Enviar email HTML con plantilla "bienvenida.html"
        emailService.enviarEmailHtml(
                usuario.getGmail(),
                "Verifica tu cuenta",
                variables,
                "email"  // nombre del archivo de la plantilla sin extensión
        );

        return ResponseEntity.ok("Usuario registrado correctamente. Verifica tu email para activar la cuenta.");
    }


    @GetMapping("/validate")
    public ResponseEntity<?> validarEmail(@RequestParam String token) {
        var tokenOpt = tokenService.validarToken(token);

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Token inválido o expirado.");
        }

        var usuarioEntity = tokenOpt.get().getUsuario();
        var usuario = UsuarioMapper.toDomain(usuarioEntity);
        usuario.setVerificado(true);
        usuarioService.actualizarUsuario(usuario);

        return ResponseEntity.ok("Usuario validado correctamente.");
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