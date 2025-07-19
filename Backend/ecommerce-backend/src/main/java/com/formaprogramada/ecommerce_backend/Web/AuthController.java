package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.Email.EmailService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Jwt.JwtTokenService;
import com.formaprogramada.ecommerce_backend.Domain.Service.TokenVerificacion.TokenVerificacionService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Usuario.UsuarioService;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.AuthRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.AuthResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth.RefreshTokenRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ResetPassword.ResetPasswordConfirmRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ResetPassword.ResetPasswordRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.UsuarioRepositoryImpl;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioActualizarMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioRegistroMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtSpecialTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final JwtSpecialTokenService jwtSpecialTokenService;
    private final JpaUsuarioRepository jpaUsuarioRepository;
    private final JwtTokenService jwtTokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;
    UsuarioMapper mapper = new UsuarioMapper();
    UsuarioActualizarMapper mapper2= new UsuarioActualizarMapper();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioRegistroRequest request) {
        if (usuarioService.existePorGmail(request.getGmail())) {
            return ResponseEntity.badRequest().body("El gmail ya está registrado");
        }

        var usuario = UsuarioRegistroMapper.toDomain(request);

        usuario.setPermiso(0);
        usuario.setVerificado(false);

        usuario = usuarioService.registrarUsuario(usuario);

        // Delegar todo el envío de email al EmailService
        emailService.enviarEmailVerificacion(usuario);

        return ResponseEntity.ok("Usuario registrado correctamente. Verifica tu email para activar la cuenta.");
    }


    @GetMapping("/validate")
    public ResponseEntity<?> validarEmail(@RequestParam String token) {
        var tokenOpt = tokenService.validarToken(token);

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Token inválido o expirado.");
        }

        var usuarioEntity = tokenOpt.get().getUsuario();
        var usuario = mapper.toDomain(usuarioEntity);
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

            AuthResponse tokens = jwtTokenService.generarTokens(userDetails);
            return ResponseEntity.ok(tokens);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse tokens = jwtTokenService.refrescarTokens(request.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }


    @PostMapping("/reset-password-request")
    public ResponseEntity<?> solicitarRestablecerPassword(@RequestBody ResetPasswordRequest request) {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El email es obligatorio");
        }

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorGmail(email.trim());

        // Por seguridad respondemos igual, si no existe usuario no hacemos nada
        if (usuarioOpt.isPresent()) {
            try {
                emailService.solicitarRestablecerPassword(email.trim());
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar email");
            }
        }

        return ResponseEntity.ok("Si el email existe, se envió un enlace para restablecer la contraseña");
    }
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmarRestablecerPassword(@RequestBody ResetPasswordConfirmRequest request) {
        String token = request.getToken();
        String nuevaPassword = request.getNuevaPassword();

        if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía");
        }

        String email = jwtSpecialTokenService.getSubjectFromToken(token);

        if (!jwtSpecialTokenService.validateResetPasswordToken(token, email)) {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }

        UsuarioEntity usuario = jpaUsuarioRepository.findByGmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String passwordHasheada = passwordEncoder.encode(nuevaPassword);
        usuario.setPassword(passwordHasheada);
        jpaUsuarioRepository.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @PutMapping("/actualizar-usuario")
    public ResponseEntity<?> actualizarUsuario(@RequestBody UsuarioUpdatePedido request){

        var usuario = mapper2.toDomain(request);

        if (usuarioService.actualizarUsuarioAlComprar(usuario)) {
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }
}