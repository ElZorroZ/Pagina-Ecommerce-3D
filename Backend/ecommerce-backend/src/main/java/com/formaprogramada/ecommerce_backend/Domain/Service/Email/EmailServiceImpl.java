package com.formaprogramada.ecommerce_backend.Domain.Service.Email;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.TokenVerificacion.TokenVerificacionService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtSpecialTokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final JwtSpecialTokenService jwtSpecialTokenService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JpaUsuarioRepository jpaUsuarioRepository;
    private final TokenVerificacionService tokenService;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void enviarEmailHtml(String destinatario, String asunto, Map<String, Object> variables, String plantilla) {
        Context context = new Context();
        context.setVariables(variables);
        String contenidoHtml = templateEngine.process(plantilla, context);

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo", e);
        }
    }


    @Override
    public void enviarEmailHtmlConArchivos(String destinatario, String asunto, Map<String, Object> variables, String plantilla,List<MultipartFile> listaDeArchivos) {
        Context context = new Context();
        context.setVariables(variables);
        String contenidoHtml = templateEngine.process(plantilla, context);

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            for (MultipartFile archivo:listaDeArchivos) {
                if (archivo != null && !archivo.isEmpty()) {
                    helper.addAttachment(archivo.getOriginalFilename(), archivo);
                }
            }


            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo", e);
        }
    }


    @Override
    public void enviarEmailArchivos(String gmail, String nombre, List<MultipartFile> listaDeArchivos) {
        String frontendUrl = "http://localhost:5501";
        String link = frontendUrl + "/WEB/usuario/envio-compra/envio-compra.html";

        Map<String, Object> variables = Map.of(
                "nombre",nombre,
                "urlValidacion", link
        );

        // Asunto y plantilla
        String asunto = "Pedido de compra en Forma Programada";
        String plantilla = "envio-compra";

        //Enviar el email
        enviarEmailHtmlConArchivos(gmail, asunto, variables, plantilla,listaDeArchivos);
    }






    @Override
    public void enviarEmailVerificacion(Usuario usuario) {
        // Generar token de verificación
        UsuarioEntity usuarioEntity = usuarioMapper.toEntity(usuario);
        String token = tokenService.crearTokenParaUsuario(usuarioEntity).getToken();

        // URL del frontend
        String frontendUrl = "http://localhost:5501";
        String link = frontendUrl + "/WEB/usuario/validacion/validar-email.html?token=" + token;

        // Variables para la plantilla
        Map<String, Object> variables = Map.of(
                "nombre", usuario.getNombre(),
                "urlValidacion", link
        );

        // Asunto y plantilla
        String asunto = "Verifica tu cuenta";
        String plantilla = "email-verificacion";

        //Enviar el email
        enviarEmailHtml(usuario.getGmail(), asunto, variables, plantilla);
    }



    @Override
    public void solicitarRestablecerPassword(String gmail) throws IllegalArgumentException {
        UsuarioEntity usuario = jpaUsuarioRepository.findByGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String token = jwtSpecialTokenService.generateResetPasswordToken(gmail);

        String frontendUrl = "http://localhost:5500";
        String link = frontendUrl + "/WEB/usuario/confirmacion-password/confirmar-password.html?token=" + token;

        Map<String, Object> variables = new HashMap<>();
        variables.put("link", link);

        String asunto = "Restablecimiento de contraseña";

        enviarEmailHtml(gmail, asunto, variables, "reset-password");
    }

    @Override
    public void solicitarCambioEmail(String gmailActual, String nuevoEmail) throws IllegalArgumentException {
        UsuarioEntity usuario = jpaUsuarioRepository.findByGmail(gmailActual)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar que el nuevo email no esté en uso
        boolean existeNuevoEmail = jpaUsuarioRepository.existsByGmail(nuevoEmail);
        if (existeNuevoEmail) {
            throw new IllegalArgumentException("El nuevo email ya está en uso");
        }

        String token = jwtSpecialTokenService.generateConfirmEmailToken(nuevoEmail, gmailActual);

        String frontendUrl = "http://localhost:5500"; // o el puerto donde tengas tu frontend
        String link = frontendUrl + "/WEB/usuario/confirmacion/confirmar-email.html?token=" + token;

        String asunto = "Confirmación de cambio de email";

        Map<String, Object> variables = new HashMap<>();
        variables.put("link", link);

        enviarEmailHtml(nuevoEmail, asunto, variables, "confirmar-email");
    }

    public void confirmarCambioEmail(String token) {
        // Extraer datos del token
        String nuevoEmail = jwtSpecialTokenService.getSubjectFromToken(token);
        String gmailActual = jwtSpecialTokenService.getClaimFromToken(token, "gmailActual", String.class);

        if (nuevoEmail == null || gmailActual == null) {
            throw new IllegalArgumentException("Token inválido o mal formado");
        }

        // Validar token con los datos extraídos
        if (!jwtSpecialTokenService.validateConfirmEmailToken(token, nuevoEmail)) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }

        // Buscar usuario por email actual
        UsuarioEntity usuario = jpaUsuarioRepository.findByGmail(gmailActual)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar que el nuevo email no esté en uso
        if (jpaUsuarioRepository.existsByGmail(nuevoEmail)) {
            throw new IllegalArgumentException("El nuevo email ya está en uso");
        }

        // Actualizar email
        usuario.setGmail(nuevoEmail);
        jpaUsuarioRepository.save(usuario);
    }

    @Override
    public void enviarEmailConfirmacionCompra(String gmail, String nombre) {
        String frontendUrl = "http://localhost:5501";
        String link = frontendUrl + "/WEB/usuario/confirmacion-compra/confirmar-compra.html";

        Map<String, Object> variables = Map.of(
                "nombre",nombre,
                "urlValidacion", link
        );

        // Asunto y plantilla
        String asunto = "Pedido de compra en Forma Programada";
        String plantilla = "confirmar-compra";

        //Enviar el email
        enviarEmailHtml(gmail, asunto, variables, plantilla);
    }


}
