package com.formaprogramada.ecommerce_backend.Domain.Service.Email;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.TokenVerificacion.TokenVerificacionService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtSpecialTokenService;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.resource.Emailv31;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.mailjet.client.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;

import java.util.HashMap;

import com.mailjet.client.MailjetResponse;


@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final JwtSpecialTokenService jwtSpecialTokenService;
    private final TokenVerificacionService tokenService;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;


        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JpaUsuarioRepository jpaUsuarioRepository;

        @Value("${app.frontend.url}")
        private String frontendUrl;

        @Value("${mailjet.api.key}")
        private String apiKey;

        @Value("${mailjet.api.secret}")
        private String apiSecret;

        private MailjetClient client;

        @PostConstruct
        public void initClient() {
            ClientOptions options = ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(apiSecret)
                    .build();
            this.client = new MailjetClient(options);
        }

        @Override
        public void enviarEmailHtml(String destinatario, String asunto, Map<String, Object> variables, String plantilla) {
            if (destinatario == null || destinatario.isBlank()) {
                throw new IllegalArgumentException("El destinatario no puede estar vacío.");
            }

            Context context = new Context();
            context.setVariables(variables);
            String contenidoHtml = templateEngine.process(plantilla, context);

            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", "formaprogramada@gmail.com")
                                            .put("Name", "Forma Programada"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", destinatario)))
                                    .put(Emailv31.Message.SUBJECT, asunto)
                                    .put(Emailv31.Message.HTMLPART, contenidoHtml)));

            try {
                MailjetResponse response = client.post(request);
                System.out.println("✅ Correo enviado a " + destinatario + " (status " + response.getStatus() + ")");
            } catch (Exception e) {
                throw new RuntimeException("❌ Error al enviar correo con Mailjet API", e);
            }
        }


    @Override
    public void enviarEmailVerificacion(Usuario usuario) {
        // Generar token de verificación
        UsuarioEntity usuarioEntity = usuarioMapper.toEntity(usuario);
        String token = tokenService.crearTokenParaUsuario(usuarioEntity).getToken();

        String link = frontendUrl + "/usuario/validacion/validar-email.html?token=" + token;

        Map<String, Object> variables = Map.of(
                "nombre", usuario.getNombre(),
                "urlValidacion", link
        );

        String asunto = "Verifica tu cuenta";
        String plantilla = "email-verificacion";

        enviarEmailHtml(usuario.getGmail(), asunto, variables, plantilla);
    }

    @Override
    public void solicitarRestablecerPassword(String gmail) throws IllegalArgumentException {
        UsuarioEntity usuario = jpaUsuarioRepository.findByGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String token = jwtSpecialTokenService.generateResetPasswordToken(gmail);
        String link = frontendUrl + "/usuario/confirmacion-password/confirmar-password.html?token=" + token;

        Map<String, Object> variables = new HashMap<>();
        variables.put("link", link);

        String asunto = "Restablecimiento de contraseña";
        enviarEmailHtml(gmail, asunto, variables, "reset-password");
    }

    @Override
    public void solicitarCambioEmail(String gmailActual, String nuevoEmail) throws IllegalArgumentException {
        UsuarioEntity usuario = jpaUsuarioRepository.findByGmail(gmailActual)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (jpaUsuarioRepository.existsByGmail(nuevoEmail)) {
            throw new IllegalArgumentException("El nuevo email ya está en uso");
        }

        String token = jwtSpecialTokenService.generateConfirmEmailToken(nuevoEmail, gmailActual);
        String link = frontendUrl + "/usuario/confirmacion/confirmar-email.html?token=" + token;

        Map<String, Object> variables = new HashMap<>();
        variables.put("link", link);

        String asunto = "Confirmación de cambio de email";
        enviarEmailHtml(nuevoEmail, asunto, variables, "confirmar-email");
    }

    @Override
    public void confirmarCambioEmail(String token) {
        String nuevoEmail = jwtSpecialTokenService.getSubjectFromToken(token);
        String gmailActual = jwtSpecialTokenService.getClaimFromToken(token, "gmailActual", String.class);

        if (nuevoEmail == null || gmailActual == null) {
            throw new IllegalArgumentException("Token inválido o mal formado");
        }

        if (!jwtSpecialTokenService.validateConfirmEmailToken(token, nuevoEmail)) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }

        UsuarioEntity usuario = jpaUsuarioRepository.findByGmail(gmailActual)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (jpaUsuarioRepository.existsByGmail(nuevoEmail)) {
            throw new IllegalArgumentException("El nuevo email ya está en uso");
        }

        usuario.setGmail(nuevoEmail);
        jpaUsuarioRepository.save(usuario);
    }

    @Override
    public void enviarConfirmacionCompra(String gmail) {
        String link = frontendUrl + "/usuario/confirmacion-compra/confirmar-compra.html";

        Map<String, Object> variables = new HashMap<>();
        variables.put("link", link);

        String asunto = "Confirmación de compra";
        enviarEmailHtml(gmail, asunto, variables, "confirmar-compra");
    }
}
