package com.formaprogramada.ecommerce_backend.Domain.Service;

import java.util.Map;

public interface EmailService {
    void enviarEmail(String destinatario, String asunto, String cuerpo);

    void enviarEmailHtml(String destinatario, String asunto, Map<String, Object> variables, String plantilla);
}