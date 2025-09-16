package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.Email.EmailService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Jwt.JwtTokenService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Jwt.RefreshTokenExpiredException;
import com.formaprogramada.ecommerce_backend.Domain.Service.Jwt.RefreshTokenNotFoundException;
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
import com.formaprogramada.ecommerce_backend.Security.Hasher.PasswordHasher;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.CustomUserDetailsService;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtSpecialTokenService;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
    private final PasswordHasher passwordHasher;
    private final CustomUserDetailsService userDetailsService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

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
        usuario.setProveedor("LOCAL");
        usuarioService.actualizarUsuario(usuario);

        return ResponseEntity.ok("Usuario validado correctamente.");
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            log.info("Intento de login para: {}", request.getGmail());

            Optional<Usuario> optUser = usuarioRepository.buscarPorGmail(request.getGmail());
            if (optUser.isEmpty()) {
                log.warn("Usuario no encontrado: {}", request.getGmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Credenciales inválidas");
            }

            Usuario usuario = optUser.get();

            if (!usuario.isVerificado()) {
                log.warn("Usuario no verificado: {}", request.getGmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Usuario no verificado");
            }

            if (!"LOCAL".equals(usuario.getProveedor())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Usa el login con Google para este usuario");
            }

            var authToken = new UsernamePasswordAuthenticationToken(
                    request.getGmail(),
                    request.getPassword()
            );
            var auth = authManager.authenticate(authToken);
            var userDetails = (UserDetails) auth.getPrincipal();

            AuthResponse response = jwtTokenService.generarTokens(userDetails);

            log.info("Login exitoso para: {} - ID: {}", request.getGmail(), response.getUsuarioId());

            return ResponseEntity.ok(Map.of(
                    "accessToken", response.getAccessToken(),
                    "refreshToken", response.getRefreshToken(),
                    "usuarioId", response.getUsuarioId()
            ));

        } catch (AuthenticationException e) {
            log.warn("Login fallido para: {} - {}", request.getGmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales inválidas");
        } catch (Exception e) {
            log.error("Error inesperado en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        return refreshTokenWithRetry(request, 0);
    }

    /**
     * 🔄 Refresh con retry logic para manejar concurrencia
     */
    private ResponseEntity<?> refreshTokenWithRetry(RefreshTokenRequest request, int attempt) {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 100; // 100ms entre intentos

        try {
            String username = jwtService.extractUsername(request.getRefreshToken());
            log.info("Procesando refresh para usuario: {} (intento {})", username, attempt + 1);

            AuthResponse response = jwtTokenService.refrescarTokens(request.getRefreshToken());

            log.info("Refresh exitoso para usuario: {} - ID: {}", username, response.getUsuarioId());

            return ResponseEntity.ok(Map.of(
                    "accessToken", response.getAccessToken(),
                    "refreshToken", response.getRefreshToken(),
                    "usuarioId", response.getUsuarioId()
            ));

        } catch (RefreshTokenExpiredException | RefreshTokenNotFoundException e) {
            log.warn("Refresh fallido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());

        } catch (Exception e) {
            // 🔄 Retry logic para errores de concurrencia
            if (attempt < MAX_RETRIES && isConcurrencyError(e)) {
                log.warn("Error de concurrencia en refresh (intento {}), reintentando: {}",
                        attempt + 1, e.getMessage());

                try {
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1)); // Backoff exponencial
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Proceso interrumpido");
                }

                return refreshTokenWithRetry(request, attempt + 1);
            }

            log.error("Error inesperado en refresh (intento {}): {}", attempt + 1, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    /**
     * 🔍 Determina si es un error relacionado con concurrencia
     */
    private boolean isConcurrencyError(Exception e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("lock") ||
                message.contains("timeout") ||
                message.contains("deadlock") ||
                message.contains("concurrent") ||
                e instanceof OptimisticLockException ||
                e instanceof PessimisticLockException;
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
        log.info("Password actualizada para {}: {}", usuario.getGmail(), usuario.getPassword());

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

    @GetMapping("/oauth2/success")
    public void getUserInfo(HttpServletResponse response, OAuth2AuthenticationToken authentication) {
        try {
            Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
            String email = (String) attributes.get("email");
            String nombre = (String) attributes.getOrDefault("given_name", "");
            String apellido = (String) attributes.getOrDefault("family_name", "");
            Boolean verificadoGoogle = (Boolean) attributes.getOrDefault("email_verified", true);

            log.info("OAuth2 - Procesando usuario: {}", email);

            // Buscar usuario existente
            Optional<Usuario> usuarioExistente = usuarioRepository.buscarPorGmail(email);
            Usuario usuario;

            if (usuarioExistente.isPresent()) {
                // Usuario ya existe - actualizar solo datos necesarios
                usuario = usuarioExistente.get();
                log.info("OAuth2 - Usuario existente encontrado: {}", email);

                // Actualizar solo si los datos están vacíos o si es la primera vez con Google
                boolean necesitaActualizacion = false;

                if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
                    usuario.setNombre(nombre);
                    necesitaActualizacion = true;
                }

                if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
                    usuario.setApellido(apellido);
                    necesitaActualizacion = true;
                }

                // Marcar como verificado si viene de Google y no estaba verificado
                if (!usuario.isVerificado() && verificadoGoogle) {
                    usuario.setVerificado(true);
                    necesitaActualizacion = true;
                }

                // Actualizar proveedor si no estaba establecido o era diferente
                if (usuario.getProveedor() == null || !usuario.getProveedor().contains("GOOGLE")) {
                    // Si ya tenía un proveedor, agregar GOOGLE, si no, establecer GOOGLE
                    String proveedorActual = usuario.getProveedor();
                    if (proveedorActual == null || proveedorActual.trim().isEmpty()) {
                        usuario.setProveedor("GOOGLE");
                    } else if (!proveedorActual.contains("GOOGLE")) {
                        usuario.setProveedor(proveedorActual + ",GOOGLE");
                    }
                    necesitaActualizacion = true;
                }

                // Guardar solo si hay cambios
                if (necesitaActualizacion) {
                    usuario = usuarioRepository.guardar(usuario);
                    log.info("OAuth2 - Usuario actualizado: {}", email);
                }

                // NO modificamos la contraseña existente

            } else {
                // Usuario nuevo - crear con todos los datos
                log.info("OAuth2 - Creando nuevo usuario: {}", email);
                usuario = new Usuario();
                usuario.setGmail(email);
                usuario.setNombre(nombre);
                usuario.setApellido(apellido);
                usuario.setVerificado(true);
                usuario.setProveedor("GOOGLE");
                // Solo para usuarios nuevos creamos una contraseña ficticia
                String passwordFicticio = passwordHasher.hash("google123!");
                usuario.setPassword(passwordFicticio);
                usuario = usuarioRepository.guardar(usuario);
            }

            // Cargar UserDetails para generar el token correctamente
            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getGmail());
            log.info("OAuth2 - Authorities: {}", userDetails.getAuthorities());

            // Generar tokens
            AuthResponse tokens = jwtTokenService.generarTokens(userDetails);
            log.info("OAuth2 - Tokens generados para usuario ID: {}", tokens.getUsuarioId());

            if (tokens.getAccessToken() == null || tokens.getRefreshToken() == null) {
                log.error("OAuth2 - Error: tokens nulos generados");
                response.sendRedirect(frontendUrl + "/usuario/login/login.html?error=token_generation_failed");
                return;
            }

            // Construir URL de redirección con tokens
            String redirectUrl = String.format(
                    "%s/index.html?accessToken=%s&refreshToken=%s&usuarioId=%d",
                    frontendUrl,
                    URLEncoder.encode(tokens.getAccessToken(), StandardCharsets.UTF_8),
                    URLEncoder.encode(tokens.getRefreshToken(), StandardCharsets.UTF_8),
                    tokens.getUsuarioId()
            );

            log.info("OAuth2 - Redirigiendo a: {}", redirectUrl.substring(0, Math.min(redirectUrl.length(), 100)) + "...");
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 - Error en procesamiento: ", e);
            try {
                response.sendRedirect(frontendUrl + "/usuario/login/login.html?error=oauth_processing_failed");
            } catch (IOException ioException) {
                log.error("OAuth2 - Error en redirección de error: ", ioException);
            }
        }
    }
}

