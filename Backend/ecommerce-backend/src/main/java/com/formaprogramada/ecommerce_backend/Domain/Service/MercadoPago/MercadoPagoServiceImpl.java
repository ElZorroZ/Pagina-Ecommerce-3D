package com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.mercadopago.MercadoPagoConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private JpaUsuarioRepository usuarioRepository;

    @Value("${mercadopago.webhook.key}")
    private String webhookKey;

    @Value("${mercadopago.sandbox.enabled:true}")
    private boolean sandboxEnabled;

    @Value("${mercadopago.base-url:https://formaprogramada.com.ar}")
    private String baseUrl;

    // Clase interna para encapsular solo lo que necesita MercadoPago
    public static class UsuarioInfoMP {
        private String nombre;
        private String apellido;
        private String email;
        private String direccion;
        private String cp;
        private String ciudad;

        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public String getCp() { return cp; }
        public void setCp(String cp) { this.cp = cp; }
        public String getCiudad() { return ciudad; }
        public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    }

    public UsuarioInfoMP getUsuarioParaPago(int usuarioId) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));

        UsuarioInfoMP infoMP = new UsuarioInfoMP();
        infoMP.setNombre(usuario.getNombre());
        infoMP.setApellido(usuario.getApellido());
        infoMP.setEmail(usuario.getGmail());
        infoMP.setDireccion(usuario.getDireccion());
        infoMP.setCp(usuario.getCp());
        infoMP.setCiudad(usuario.getCiudad());

        return infoMP;
    }

    @Override
    public String confirmarPedido(String mercadolibreToken, String title, BigDecimal price, String pedidoId, int quantity, int usuarioId) {
        try {
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor a 0");
            }

            // Buscar usuario real
            UsuarioInfoMP usuario = getUsuarioParaPago(usuarioId);

            // Validar token
            boolean isTestToken = mercadolibreToken.startsWith("TEST-");
            boolean isProductionToken = mercadolibreToken.startsWith("APP_USR-");

            if (!isTestToken && !isProductionToken) {
                throw new IllegalArgumentException("Token inválido. Debe comenzar con TEST- o APP_USR-");
            }

            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(mercadolibreToken);

            // Crear item
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(pedidoId)
                    .title(title)
                    .quantity(quantity) // usualmente 1
                    .unitPrice(price)
                    .currencyId("ARS")
                    .categoryId("others")
                    .build();

            // URLs de retorno
            String successUrl = baseUrl + "/api/mp/pago-exitoso?pedidoId=" + pedidoId;
            String pendingUrl = baseUrl + "/api/mp/pago-pendiente?pedidoId=" + pedidoId;
            String failureUrl = baseUrl + "/api/mp/pago-fallido?pedidoId=" + pedidoId;
            String webhookUrl = baseUrl + "/api/mp/webhook";

            // Construir preferencia
            PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(PreferenceBackUrlsRequest.builder()
                            .success(successUrl)
                            .pending(pendingUrl)
                            .failure(failureUrl)
                            .build())
                    .externalReference(pedidoId)
                    .notificationUrl(webhookUrl)
                    .expires(true)
                    .expirationDateFrom(OffsetDateTime.now())
                    .expirationDateTo(OffsetDateTime.now().plusHours(24))
                    .statementDescriptor("PEDIDO " + pedidoId)
                    .payer(PreferencePayerRequest.builder()
                            .name(usuario.getNombre())
                            .surname(usuario.getApellido())
                            .email(usuario.getEmail())
                            .build());

            // Configuración según ambiente
            if (isTestToken) {
                requestBuilder.binaryMode(false).autoReturn("approved");
            } else {
                requestBuilder.binaryMode(true).autoReturn("approved");
            }

            // Crear preferencia
            PreferenceRequest preferenceRequest = requestBuilder.build();
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            if (preference.getId() == null || preference.getId().isEmpty()) {
                throw new RuntimeException("Preference creada sin ID válido");
            }

            // Guardar preference ID en pedido
            pedidoService.guardarMercadoPagoId(pedidoId, preference.getId());

            // Retornar URL de pago
            return isTestToken ? preference.getSandboxInitPoint() : preference.getInitPoint();

        } catch (Exception e) {
            throw new RuntimeException("Error procesando pago: " + e.getMessage(), e);
        }
    }



    public boolean verifySignature(String signature, Map<String, Object> payload) {
        System.out.println("🔐 Verificando webhook signature:");
        System.out.println("   - Signature recibida: " + (signature != null ? "SÍ" : "NO"));
        System.out.println("   - Webhook key configurada: " + (webhookKey != null ? "SÍ" : "NO"));
        System.out.println("   - Modo sandbox: " + sandboxEnabled);

        // 🔥 EN SANDBOX, SER MÁS PERMISIVO CON LA VERIFICACIÓN
        if (sandboxEnabled) {
            System.out.println("🧪 Modo sandbox: verificación permisiva activada");

            // Si no hay signature en sandbox, permitir
            if (signature == null) {
                System.out.println("⚠️ Sin signature en sandbox - PERMITIENDO");
                return true;
            }

            // Si no hay webhook key configurada en sandbox, permitir
            if (webhookKey == null || webhookKey.trim().isEmpty()) {
                System.out.println("⚠️ Sin webhook key en sandbox - PERMITIENDO");
                return true;
            }
        }

        // Verificación normal para producción o cuando hay signature
        if (signature == null || webhookKey == null) {
            System.out.println("❌ Signature o webhook key faltante en producción");
            return false;
        }

        try {
            String payloadString = new ObjectMapper().writeValueAsString(payload);
            SecretKeySpec secretKey = new SecretKeySpec(webhookKey.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payloadString.getBytes());
            String computedSignature = "sha256=" + Base64.getEncoder().encodeToString(hash);

            System.out.println("🔐 Signature esperada: " + computedSignature);
            System.out.println("🔐 Signature recibida: " + signature);

            boolean isValid = computedSignature.equals(signature);
            System.out.println("🔐 Signature válida: " + isValid);

            return isValid;

        } catch (Exception e) {
            System.err.println("❌ Error verificando signature: " + e.getMessage());
            e.printStackTrace();

            // En sandbox, si hay error de verificación, permitir
            if (sandboxEnabled) {
                System.out.println("🧪 Error en sandbox - PERMITIENDO por desarrollo");
                return true;
            }

            return false;
        }
    }
}