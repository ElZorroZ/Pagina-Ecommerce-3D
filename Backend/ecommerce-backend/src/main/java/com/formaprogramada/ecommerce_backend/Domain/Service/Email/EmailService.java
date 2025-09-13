package com.formaprogramada.ecommerce_backend.Domain.Service.Email;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EmailService {
    void solicitarRestablecerPassword(String gmail) throws IllegalArgumentException;

    void solicitarCambioEmail(String gmailActual, String nuevoEmail) throws IllegalArgumentException;

    void enviarEmailVerificacion(Usuario usuario);

    void enviarEmailHtml(String destinatario, String asunto, Map<String, Object> variables, String plantilla);

    void confirmarCambioEmail(String token);

    void enviarEmailConfirmacionCompra(String gmail, String nonmbre);
    
}