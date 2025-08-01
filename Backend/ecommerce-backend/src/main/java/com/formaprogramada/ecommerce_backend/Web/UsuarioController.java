package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Service.Colaborador.ColaboradorService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Email.EmailService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Usuario.UsuarioService;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CambioEmail.CambioEmailRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CambioPassword.CambioPasswordRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Colaborador.ColaboradorDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioGetUpdateResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtSpecialTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private final JwtSpecialTokenService jwtSpecialTokenService;
    private final ColaboradorService colaboradorService;
    // GET para obtener datos completos del usuario por gmail
    @GetMapping("/{gmail}")
    public ResponseEntity<UsuarioGetUpdateResponse> obtenerUsuario(@PathVariable String gmail) {
        return usuarioService.buscarPorGmail(gmail)
                .map(usuario -> {
                    UsuarioGetUpdateResponse dto = UsuarioMapper.toGetUpdateResponseFromDomain(usuario);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }



    // PUT para actualizar parcialmente usuario por gmail
    @PutMapping("/{gmail}")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable String gmail,
            @Valid @RequestBody UsuarioUpdate usuarioUpdate
    ) {
        try {
            Usuario actualizado = usuarioService.actualizarUsuarioPorGmail(gmail, usuarioUpdate);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT para cambiar contraseña (envía email con link de restablecimiento)
    @PutMapping("/{gmail}/password")
    public ResponseEntity<?> cambiarPassword(@PathVariable String gmail) {
        try {
            emailService.solicitarRestablecerPassword(gmail);
            return ResponseEntity.ok("Se envió un email para restablecer la contraseña.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{gmail}/password-directo")
    public ResponseEntity<?> cambiarPasswordDirecto(
            @PathVariable String gmail,
            @RequestBody CambioPasswordRequest request) {
        try {
            usuarioService.cambiarPassword(gmail, request);
            return ResponseEntity.ok("Contraseña actualizada correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{gmail}/email")
    public ResponseEntity<?> cambiarEmail(@PathVariable String gmail, @RequestBody CambioEmailRequest request) {
        String nuevoEmail = request.getNuevoEmail();
        if (nuevoEmail == null || nuevoEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("El nuevo email es requerido");
        }

        try {
            emailService.solicitarCambioEmail(gmail, nuevoEmail);
            return ResponseEntity.ok("Se envió un email para confirmar el cambio de email.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // GET para confirmar cambio de email con token
    @GetMapping("/confirmar-email")
    public ResponseEntity<?> confirmarCambioEmail(@RequestParam String token) {
        try {
            emailService.confirmarCambioEmail(token);
            return ResponseEntity.ok("Email cambiado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/colaboradores")
    public ResponseEntity<?> alternarPermisoColaborador(@RequestBody ColaboradorDTO dto) {
        try {
            colaboradorService.alternarPermiso(dto.getGmail());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @GetMapping("/colaboradores")
    public ResponseEntity<List<ColaboradorDTO>> obtenerColaboradores() {
        List<ColaboradorDTO> colaboradores = colaboradorService.obtenerColaboradores();
        return ResponseEntity.ok(colaboradores);
    }




}

